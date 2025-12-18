package gg.kuken.feature.blueprint

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface BlueprintSpecSource {
    @JvmInline
    @SerialName("local")
    value class Local(
        val filePath: String,
    ) : BlueprintSpecSource

    @JvmInline
    @SerialName("remote")
    value class Remote(
        val url: String,
    ) : BlueprintSpecSource
}
