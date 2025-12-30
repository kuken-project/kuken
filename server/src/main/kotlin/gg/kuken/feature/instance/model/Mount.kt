package gg.kuken.feature.instance.model

import kotlinx.serialization.Serializable

@Serializable
data class InstanceRuntimeMount(
    val type: String,
    val source: String,
    val destination: String,
    val readonly: Boolean,
)
