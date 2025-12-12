package gg.kuken.feature.unit.http.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

// Docker Image Specification v1.2.0 - https://github.com/moby/moby/blob/master/image/spec/v1.2.md
private const val IMAGE_LENGTH = 128
private const val IMAGE_REGEX = ".*[a-zA-Z0-9_.-]"

@Serializable
internal data class CreateUnitRequest(
    @field:NotBlank(message = "Name must be provided")
    @field:Size(
        min = 2,
        max = 64,
        message = "Name must have a minimum length of {min} and at least {max} characters.",
    )
    val name: String = "",
    @field:NotNull(message = "Blueprint must be provided.")
    val blueprint: Uuid,
    @field:NotBlank(message = "Image must be provided.")
    @field:Size(max = IMAGE_LENGTH)
    @field:Pattern(regexp = IMAGE_REGEX)
    val image: String = "",
    val network: Network? = null,
    val options: Map<String, String> = emptyMap(),
) {
    @Serializable
    internal data class Network(
        val host: String?,
        val port: UShort?,
    )
}
