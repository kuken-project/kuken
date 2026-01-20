package gg.kuken.feature.instance.http.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class ExecuteCommandResponse(
    val exitCode: Int?,
)
