package gg.kuken.feature.rbac.entity

import gg.kuken.feature.rbac.model.PermissionPolicy
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp

object RolePermissionsTable : UUIDTable("role_permissions") {
    val roleId = reference("role_id", RolesTable, onDelete = ReferenceOption.CASCADE)
    val permissionId = reference("permission_id", PermissionsTable, onDelete = ReferenceOption.CASCADE)
    val policy = enumerationByName<PermissionPolicy>("policy", 50).default(PermissionPolicy.AllowAll)
    val grantedAt = timestamp("granted_at")

    init {
        uniqueIndex(roleId, permissionId)
    }
}
