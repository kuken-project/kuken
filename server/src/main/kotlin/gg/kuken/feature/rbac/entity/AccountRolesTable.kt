package gg.kuken.feature.rbac.entity

import gg.kuken.feature.account.entity.AccountTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp

object AccountRolesTable : UUIDTable("account_roles") {
    val accountId = uuid("account_id").references(AccountTable.id, onDelete = ReferenceOption.CASCADE)
    val roleId = reference("role_id", RolesTable, onDelete = ReferenceOption.CASCADE)
    val grantedAt = timestamp("granted_at")
    val grantedBy = uuid("granted_by").references(AccountTable.id, onDelete = ReferenceOption.CASCADE)
    val expiresAt = timestamp("expires_at").nullable()

    init {
        index(isUnique = false, accountId, roleId)
        index(isUnique = false, expiresAt)
    }
}
