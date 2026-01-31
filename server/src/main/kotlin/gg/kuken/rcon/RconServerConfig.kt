package gg.kuken.rcon

import kotlinx.serialization.Serializable

@Serializable
data class RconServerConfig(
    val host: String,
    val port: Int,
    val password: String,
    val name: String,
    val timeoutMs: Long,
)
