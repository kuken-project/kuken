package gg.kuken.features.auth.http.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(@SerialName("token") val token: String)
