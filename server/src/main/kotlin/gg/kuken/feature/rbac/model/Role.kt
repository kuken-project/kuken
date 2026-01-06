package gg.kuken.feature.rbac.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Role(
    val id: Uuid,
    val name: String,
    val description: String,
    val isSystem: Boolean, // Cannot be deleted or modified by regular users
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class RolePermission(
    val id: Uuid,
    val roleId: Uuid,
    val permissionId: Uuid,
    val policy: PermissionPolicy = PermissionPolicy.AllowAll,
    val grantedAt: Instant,
)
