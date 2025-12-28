package gg.kuken.feature.rbac.entity

import gg.kuken.feature.rbac.model.PermissionPolicy
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp

object AccountPermissionsTable : UUIDTable("account_permissions") {
    val accountId = uuid("account_id")
    val permissionId = reference("permission_id", PermissionsTable, onDelete = ReferenceOption.CASCADE)
    val policy = enumerationByName<PermissionPolicy>("policy", 50).default(PermissionPolicy.AllowAll)
    val grantedAt = timestamp("granted_at")
    val grantedBy = uuid("granted_by")
    val expiresAt = timestamp("expires_at").nullable()

    init {
        index(isUnique = false, accountId, permissionId)
        index(isUnique = false, expiresAt)
    }
}
