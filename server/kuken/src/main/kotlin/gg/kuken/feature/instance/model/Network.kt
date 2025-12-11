package gg.kuken.feature.instance.model

import kotlinx.serialization.Serializable

@Serializable
data class InstanceRuntimeNetwork(
    val ipV4Address: String,
    val hostname: String?,
    val networks: List<InstanceRuntimeSingleNetwork>,
)

@Serializable
data class InstanceRuntimeSingleNetwork(
    val id: String,
    val name: String,
    val ipv4Address: String?,
    val ipv6Address: String?,
)
