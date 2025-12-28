package gg.kuken.feature.rbac.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class PermissionResourceRule(
    val id: Uuid,
    val accountPermissionId: Uuid? = null,
    val rolePermissionId: Uuid? = null,
    val resourceType: ResourceType,
    val resourceId: Uuid,
    val createdAt: Instant,
)
