

package gg.kuken.feature.auth.http.dto

import gg.kuken.feature.rbac.model.PermissionName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class VerifyResponse(
    @SerialName("accountId") val id: String,
    @SerialName("email") val email: String,
    @SerialName("createdAt") val createdAt: Instant,
    @SerialName("updatedAt") val updatedAt: Instant,
    @SerialName("lastLoggedInAt") val lastLoggedInAt: Instant?,
    @SerialName("permissions") val permissions: List<PermissionName>,
)
