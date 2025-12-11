package gg.kuken.feature.instance

import kotlinx.serialization.Serializable

@Serializable
data class InstanceRuntimeMount(
    val type: String,
    val target: String,
    val destination: String,
    val readonly: Boolean,
)
