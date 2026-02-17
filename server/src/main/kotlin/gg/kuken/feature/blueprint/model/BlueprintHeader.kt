package gg.kuken.feature.blueprint.model

import kotlinx.serialization.Serializable

@Serializable
data class BlueprintHeader(
    val name: String,
    val version: String,
    val url: String,
)
