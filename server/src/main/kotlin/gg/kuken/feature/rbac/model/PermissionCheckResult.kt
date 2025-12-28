package gg.kuken.feature.rbac.model

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class PermissionCheckResult(
    val hasPermission: Boolean,
    val source: PermissionSource?,
    val sourceId: Uuid?,
    val sourceName: String?,
    val policy: PermissionPolicy?,
    val appliedRule: Uuid?,
)
