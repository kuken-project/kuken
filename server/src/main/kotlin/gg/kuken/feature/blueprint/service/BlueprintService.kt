package gg.kuken.feature.blueprint.service

import gg.kuken.KukenConfig
import gg.kuken.feature.account.IdentityGeneratorService
import gg.kuken.feature.blueprint.BlueprintNotFoundException
import gg.kuken.feature.blueprint.BlueprintSpecProvider
import gg.kuken.feature.blueprint.BlueprintSpecSource
import gg.kuken.feature.blueprint.entity.BlueprintEntity
import gg.kuken.feature.blueprint.fromString
import gg.kuken.feature.blueprint.model.Blueprint
import gg.kuken.feature.blueprint.model.BlueprintHeader
import gg.kuken.feature.blueprint.model.BlueprintStatus
import gg.kuken.feature.blueprint.processor.BlueprintConverter
import gg.kuken.feature.blueprint.processor.BlueprintProcessor
import gg.kuken.feature.blueprint.processor.NoopBlueprintResourceReader
import gg.kuken.feature.blueprint.processor.ResolveBlueprintInputDefinitions
import gg.kuken.feature.blueprint.processor.ResolvedBlueprint
import gg.kuken.feature.blueprint.processor.get
import gg.kuken.feature.blueprint.processor.toBlueprintHeader
import gg.kuken.feature.blueprint.repository.BlueprintRepository
import gg.kuken.feature.instance.data.repository.InstanceRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import org.apache.logging.log4j.LogManager
import org.pkl.core.ModuleSource
import org.pkl.core.PModule
import org.pkl.core.PObject
import java.net.URI
import java.nio.file.Path
import java.util.Base64
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

class BlueprintService(
    private val blueprintRepository: BlueprintRepository,
    private val blueprintSpecProvider: BlueprintSpecProvider,
    private val identityGeneratorService: IdentityGeneratorService,
    private val blueprintConverter: BlueprintConverter,
    private val blueprintProcessor: BlueprintProcessor,
    private val kukenConfig: KukenConfig,
    private val httpClient: HttpClient,
    private val instanceRepository: InstanceRepository,
) {
    private val logger = LogManager.getLogger(BlueprintService::javaClass)

    private companion object {
        private val json: Json =
            Json {
                coerceInputValues = false
            }
    }

    suspend fun listBlueprints(): List<Blueprint> = blueprintRepository.findAll().map(::toModel)

    suspend fun getBlueprint(id: Uuid): Blueprint =
        blueprintRepository.find(id)?.let(::toModel)
            ?: throw BlueprintNotFoundException()

    private suspend fun saveBlueprint(
        source: BlueprintSpecSource,
        header: BlueprintHeader,
        icon: ByteArray? = null,
    ): Blueprint {
        val entity =
            blueprintRepository.create(
                id = identityGeneratorService.generate(),
                header = header,
                origin = source.uri,
                status = BlueprintStatus.Unknown,
                createdAt = Clock.System.now(),
                icon = icon,
            )

        return toModel(entity)
    }

    suspend fun importBlueprint(source: BlueprintSpecSource): Blueprint {
        logger.debug("Importing {}", source)

        val fileContents = blueprintSpecProvider.provide(source)
        val module =
            blueprintConverter.eval(
                source = ModuleSource.text(fileContents),
                readers = listOf(NoopBlueprintResourceReader),
            )

        val header = module.toBlueprintHeader()
        val iconBytes = downloadIconBytes(source, module)

        val existing = blueprintRepository.findByOrigin(source.uri)
        val saved =
            if (existing != null) {
                logger.debug("Blueprint with origin {} already exists (id={}), updating", source.uri, existing.id)
                blueprintRepository.update(existing.id.value.toKotlinUuid(), header, icon = iconBytes)
                instanceRepository.markOutdatedByBlueprintId(existing.id.value.toKotlinUuid())
                toModel(existing)
            } else {
                saveBlueprint(source, header, icon = iconBytes)
            }

        supervisorScope {
            val saveFile =
                async {
                    saveResource(
                        blueprintId = saved.id,
                        blueprintSource = saved.origin,
                        resourceOrigin = source.uri,
                    )
                }

            listOf(saveFile).awaitAll()
        }

        return saved
    }

    private suspend fun downloadIconBytes(
        blueprintSource: BlueprintSpecSource,
        module: PModule,
    ): ByteArray? {
        val iconRef = module.get<PObject?>("assets")?.get<String?>("icon") ?: return null
        val resourceSource = with(json) { BlueprintSpecSource.fromString(iconRef) }

        return when (resourceSource) {
            is BlueprintSpecSource.Local -> {
                if (blueprintSource is BlueprintSpecSource.Local) {
                    val baseDir = Path(blueprintSource.filePath).toAbsolutePath().normalize()
                    val resourceFile = baseDir.resolve(resourceSource.filePath).normalize()
                    if (!resourceFile.startsWith(baseDir) || !resourceFile.exists()) null
                    else resourceFile.readBytes()
                } else {
                    val safePath =
                        resourceSource.filePath
                            .split("/")
                            .filter { it != ".." && it != "." }
                            .joinToString("/")
                    val baseUrl = blueprintSource.uri.substringBeforeLast("/") + "/"
                    val resourceUrl = URI(baseUrl).resolve(safePath).toURL()
                    downloadRemoteResourceFile(resourceUrl.toString()) { _, contents, _ -> contents }
                }
            }

            is BlueprintSpecSource.Remote -> {
                val resourceUrl = URI(resourceSource.uri).toURL()
                downloadRemoteResourceFile(resourceUrl.toString()) { _, contents, _ -> contents }
            }
        }
    }

    internal sealed class SaveResourceResult {
        data object LocalEscapingBaseDirectory : SaveResourceResult()

        data object LocalFileNotFound : SaveResourceResult()

        data class Success(
            val filename: String,
            val source: BlueprintSpecSource.Local,
        ) : SaveResourceResult()
    }

    internal suspend fun saveResource(
        blueprintId: Uuid,
        blueprintSource: BlueprintSpecSource,
        resourceOrigin: String,
    ): SaveResourceResult {
        val resourceSource = with(json) { BlueprintSpecSource.fromString(resourceOrigin) }
        val directory by lazy {
            kukenConfig.engine.blueprintsDataDirectory
                .resolve(blueprintId.toString())
                .createDirectories()
        }

        when (resourceSource) {
            is BlueprintSpecSource.Local -> {
                if (blueprintSource is BlueprintSpecSource.Local) {
                    val baseDir = Path(blueprintSource.filePath).toAbsolutePath().normalize()
                    val resourceFile = baseDir.resolve(resourceSource.filePath).normalize()

                    // When blueprint is being imported from the local file system then
                    // a local resource must be located in the same directory as the blueprint file
                    // Ensure the resolved path doesn't escape the base directory
                    if (!resourceFile.startsWith(baseDir)) {
                        logger.error("Blueprint resource path escapes blueprint base directory: {}", resourceSource.filePath)
                        return SaveResourceResult.LocalEscapingBaseDirectory
                    }

                    if (!resourceFile.exists()) {
                        logger.error("Blueprint resource local file not found: {}", resourceSource.filePath)
                        return SaveResourceResult.LocalFileNotFound
                    }

                    // Do nothing here, just validate the file
                    // Maybe make a copy of the resource? Maybe.
                    val newSource = BlueprintSpecSource.Local(resourceFile.absolutePathString())
                    return SaveResourceResult.Success(resourceFile.name, newSource)
                } else {
                    val safePath =
                        resourceSource.filePath
                            .split("/")
                            .filter { it != ".." && it != "." }
                            .joinToString("/")

                    val baseUrl = blueprintSource.uri.substringBeforeLast("/") + "/"
                    val resourceUrl = URI(baseUrl).resolve(safePath).toURL()
                    val fileLocation = directory.resolve(safePath)
                    fileLocation.parent.createDirectories()

                    val filename =
                        downloadRemoteResourceFile(resourceUrl.toString()) { _, contents, filename ->
                            fileLocation.writeBytes(contents)
                            filename
                        }

                    return SaveResourceResult.Success(filename, BlueprintSpecSource.Local(fileLocation.absolutePathString()))
                }
            }

            is BlueprintSpecSource.Remote -> {
                val resourceUrl = URI(resourceSource.uri).toURL()

                val (fileLocation, filename) =
                    downloadRemoteResourceFile(resourceUrl.toString()) { _, contents, filename ->
                        directory.resolve(filename).also { file -> file.writeBytes(contents) } to filename
                    }

                return SaveResourceResult.Success(filename, BlueprintSpecSource.Local(fileLocation.absolutePathString()))
            }
        }
    }

    private suspend inline fun <T> downloadRemoteResourceFile(
        url: String,
        crossinline payload: (Path, ByteArray, String) -> T,
    ): T {
        logger.debug("Downloading blueprint resource...: {}", url)
        val outputFile = KukenConfig.tempFile()

        try {
            val httpResponse: HttpResponse =
                httpClient.get(url) {
                    onDownload { bytesSentTotal, contentLength ->
                        logger.debug("Received {} bytes from {}", bytesSentTotal, contentLength)
                    }
                }

            val filename =
                httpResponse.headers[HttpHeaders.ContentDisposition]
                    ?.substringAfter("filename=")
                    ?.removeSurrounding("\"")
                    ?: url.substringAfterLast("/")

            val responseBody: ByteArray = httpResponse.body()
            outputFile.writeBytes(responseBody)
            logger.debug("Download finished: {} from {}", filename, url)
            return payload(outputFile, responseBody, filename)
        } finally {
            outputFile.deleteIfExists()
        }
    }

    suspend fun resolveBlueprintPartial(blueprintId: Uuid): ResolveBlueprintInputDefinitions {
        val blueprint = getBlueprint(blueprintId)
        val contents = blueprintSpecProvider.provide(blueprint.origin)
        val resolved = blueprintProcessor.processPartial(contents, listOf(NoopBlueprintResourceReader))

        return resolved
    }

    private suspend fun processBlueprint(
        origin: String,
        blueprint: ResolvedBlueprint,
        dockerImage: String,
    ) {
        logger.debug("Resolving blueprint from {} with {}", origin, dockerImage)

        if (blueprint.resources.isNotEmpty()) {
            logger.debug("Downloading required resources...")

            blueprint.resources.forEach { resource ->
                logger.debug("Downloading \"{}\" from {}...", resource.name, origin)

                if (resource.source.startsWith("file://")) {
                    val resourceUrl = origin.substringBeforeLast("/") + "/" + resource.source.replace("file://", "")

                    val client = HttpClient(CIO)
                    val outputFile = KukenConfig.tempDir(Paths.get("resources", resource.name)).toFile()
                    outputFile.parentFile.mkdirs()
                    outputFile.createNewFile()

                    try {
                        val httpResponse: HttpResponse =
                            client.get(resourceUrl) {
                                onDownload { bytesSentTotal, contentLength ->
                                    logger.debug("Received $bytesSentTotal bytes from $contentLength")
                                }
                            }

                        val responseBody: ByteArray = httpResponse.body()
                        outputFile.writeBytes(responseBody)
                        logger.debug("Download of {} finished: {}", resource.name, outputFile.absolutePath)
                    } catch (e: Exception) {
                        logger.debug("Download failed: ${e.message}")
                    } finally {
                        client.close()
                    }
                }
            }
        }
    }

    private fun toModel(entity: BlueprintEntity) =
        Blueprint(
            id = entity.id.value.toKotlinUuid(),
            origin = json.decodeFromJsonElement(JsonPrimitive(entity.origin)),
            header =
                BlueprintHeader(
                    name = entity.name,
                    url = entity.url,
                    version = entity.version,
                    author = entity.author,
                    icon = entity.icon?.bytes?.let { Base64.getEncoder().encodeToString(it) },
                ),
            createdAt = entity.createdAt,
            status = entity.status,
        )
}
