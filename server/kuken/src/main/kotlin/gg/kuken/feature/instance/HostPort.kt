package gg.kuken.feature.instance

import kotlinx.serialization.Serializable

@Serializable
data class HostPort(
    val host: String,
    val port: Short,
)
