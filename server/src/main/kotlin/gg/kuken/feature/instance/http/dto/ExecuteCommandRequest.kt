package gg.kuken.feature.instance.http.dto

import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable

@Serializable
internal data class ExecuteCommandRequest(
    @field:NotBlank
    val command: String,
)
