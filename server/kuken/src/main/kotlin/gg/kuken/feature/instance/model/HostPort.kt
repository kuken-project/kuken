package gg.kuken.feature.instance.model

import kotlinx.serialization.Serializable

@Serializable
data class HostPort(
    val host: String,
    val port: Short,
)
