package gg.kuken.feature.blueprint

import gg.kuken.KukenConfig
import gg.kuken.feature.account.IdentityGeneratorService
import gg.kuken.feature.blueprint.entity.BlueprintEntity
import gg.kuken.feature.blueprint.model.Blueprint
import gg.kuken.feature.blueprint.model.BlueprintHeader
import gg.kuken.feature.blueprint.model.BlueprintStatus
import gg.kuken.feature.blueprint.processor.BlueprintConverter
import gg.kuken.feature.blueprint.processor.NoopBlueprintResourceReader
import gg.kuken.feature.blueprint.processor.ResolveBlueprintInputDefinitions
import gg.kuken.feature.blueprint.processor.ResolvedBlueprint
import gg.kuken.feature.blueprint.repository.BlueprintRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import org.apache.logging.log4j.LogManager
import org.pkl.core.ModuleSource
import org.pkl.core.PObject
import java.nio.file.Paths
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

class BlueprintService(
    private val blueprintRepository: BlueprintRepository,
    private val blueprintSpecProvider: BlueprintSpecProvider,
    private val identityGeneratorService: IdentityGeneratorService,
    private val blueprintConverter: BlueprintConverter,
    private val blueprintProcessor: BlueprintProcessor,
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
    ): Blueprint {
        val entity =
            blueprintRepository.create(
                id = identityGeneratorService.generate(),
                header = header,
                origin = source.uri,
                status = BlueprintStatus.UpToDate,
                createdAt = Clock.System.now(),
            )

        return toModel(entity)
    }

    suspend fun importBlueprint(source: BlueprintSpecSource): Blueprint {
        logger.debug("Importing {}", source)

        val fileContents = blueprintSpecProvider.provide(source)
        val converted =
            blueprintConverter.eval(
                source = ModuleSource.text(fileContents),
                readers = listOf(NoopBlueprintResourceReader),
            )

        val header =
            BlueprintHeader(
                name = converted.getProperty("name") as String,
                version = converted.getProperty("version") as String,
                url = converted.getProperty("url") as String,
                author = converted.getProperty("author") as String,
                assets =
                    converted.getProperty("assets").let { it as PObject }.let { assets ->
                        BlueprintHeader.Assets(
                            icon = json.decodeFromJsonElement(JsonPrimitive(assets.getProperty("icon") as String)),
                        )
                    },
            )

        return saveBlueprint(source, header)
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
                    logger.debug("Downloading {}", resourceUrl)

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
                    author = entity.author,
                    url = entity.url,
                    version = entity.version,
                    assets =
                        BlueprintHeader.Assets(
                            icon = json.decodeFromJsonElement(JsonPrimitive(entity.assetsIcon)),
                        ),
                ),
            createdAt = entity.createdAt,
            status = entity.status,
        )
}
