package gg.kuken.feature.blueprint

import gg.kuken.feature.blueprint.model.BlueprintSpec
import gg.kuken.feature.blueprint.parser.BlueprintParser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import java.io.File
import java.nio.channels.UnresolvedAddressException

interface BlueprintSpecProvider {
    val providerId: String

    suspend fun provide(source: BlueprintSpecSource): BlueprintSpec
}

data class CombinedBlueprintSpecProvider(
    val providers: List<BlueprintSpecProvider>,
) : BlueprintSpecProvider {
    override val providerId: String
        get() = error("Cannot get id from CombinedBlueprintResourceProvider")

    override suspend fun provide(source: BlueprintSpecSource): BlueprintSpec {
        for (provider in providers) {
            try {
                return provider.provide(source)
            } catch (_: UnsupportedBlueprintSpecSource) {
                continue
            }
        }

        throw NoMatchingBlueprintSpecProviderException()
    }
}

@JvmInline
value class RemoteBlueprintSpecSource(
    val url: String,
) : BlueprintSpecSource

class RemoteBlueprintSpecProvider(
    private val parser: BlueprintParser,
) : BlueprintSpecProvider {
    private val httpClient: HttpClient = HttpClient()
    override val providerId: String get() = "remote"

    override suspend fun provide(source: BlueprintSpecSource): BlueprintSpec {
        if (source !is RemoteBlueprintSpecSource) {
            throw UnsupportedBlueprintSpecSource()
        }

        val response =
            try {
                httpClient.get(source.url)
            } catch (_: UnresolvedAddressException) {
                throw BlueprintSpecNotFound()
            }

        val contents: String = response.body()
        return parser.parse(contents)
    }
}

@JvmInline
value class LocalBlueprintSpecSource(
    val filePath: String,
) : BlueprintSpecSource

class LocalBlueprintSpecProvider(
    private val parser: BlueprintParser,
) : BlueprintSpecProvider {
    override val providerId: String get() = "remote"

    override suspend fun provide(source: BlueprintSpecSource): BlueprintSpec {
        if (source !is LocalBlueprintSpecSource) {
            throw UnsupportedBlueprintSpecSource()
        }

        val contents = File(source.filePath).readText()
        return parser.parse(contents)
    }
}
