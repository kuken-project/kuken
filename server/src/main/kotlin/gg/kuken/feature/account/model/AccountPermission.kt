package gg.kuken.feature.account.model

import gg.kuken.feature.rbac.model.PermissionPolicy
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class AccountPermission(
    val id: Uuid,
    val accountId: Uuid,
    val permissionId: Uuid,
    val policy: PermissionPolicy = PermissionPolicy.AllowAll,
    val grantedAt: Instant,
    val expiresAt: Instant? = null,
)
