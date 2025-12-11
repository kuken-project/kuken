package gg.kuken.feature.account.http.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email
    val email: String = "",
    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(
        min = 8,
        message = "Password must have a minimum length of 8",
    )
    val password: String = "",
)
