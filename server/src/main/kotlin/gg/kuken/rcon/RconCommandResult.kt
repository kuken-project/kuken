package gg.kuken.rcon

import kotlinx.serialization.Serializable

@Serializable
data class RconCommandResult(
    val success: Boolean,
    val response: String,
    val executionTimeMs: Long,
    val serverName: String? = null,
    val error: String? = null,
)
