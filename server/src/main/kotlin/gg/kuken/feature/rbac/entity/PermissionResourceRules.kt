package gg.kuken.feature.rbac.entity

import gg.kuken.feature.rbac.entity.PermissionResourceRulesTable.createdAt
import gg.kuken.feature.rbac.model.ResourceType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.insert
import java.util.UUID
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

object PermissionResourceRulesTable : UUIDTable("permission_resource_rules") {
    val accountPermissionId = reference("account_permission_id", AccountPermissionsTable, onDelete = ReferenceOption.CASCADE).nullable()
    val rolePermissionId = reference("role_permission_id", RolePermissionsTable, onDelete = ReferenceOption.CASCADE).nullable()
    val resourceType = enumerationByName<ResourceType>("resource_type", 50)
    val resourceId = uuid("resource_id")
    val createdAt = timestamp("created_at")

    init {
        index(isUnique = false, accountPermissionId)
        index(isUnique = false, rolePermissionId)
        index(isUnique = false, resourceType, resourceId)
    }
}
