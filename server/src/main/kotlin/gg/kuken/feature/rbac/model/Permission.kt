package gg.kuken.feature.rbac.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

typealias PermissionName = String

@Serializable
data class Permission(
    val id: Uuid,
    val name: PermissionName,
    val resource: ResourceType,
    val action: PermissionAction,
    val description: String,
    val createdAt: Instant,
)

enum class PermissionAction {
    Create,
    Read,
    Update,
    Delete,
    Execute,
    Manage,
}

@Serializable
enum class PermissionPolicy {
    AllowAll,
    AllowSpecific,
    DenySpecific,
}

@Serializable
enum class PermissionSource {
    Direct,
    Role,
}
