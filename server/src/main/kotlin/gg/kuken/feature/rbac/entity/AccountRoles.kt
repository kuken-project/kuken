package gg.kuken.feature.rbac.entity

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp

object AccountRolesTable : UUIDTable("account_roles") {
    val accountId = uuid("account_id")
    val roleId = reference("role_id", RolesTable, onDelete = ReferenceOption.CASCADE)
    val grantedAt = timestamp("granted_at")
    val grantedBy = uuid("granted_by")
    val expiresAt = timestamp("expires_at").nullable()

    init {
        index(isUnique = false, accountId, roleId)
        index(isUnique = false, expiresAt)
    }
}
