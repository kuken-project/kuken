package gg.kuken.feature.rbac.entity

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp

object RolesTable : UUIDTable("roles") {
    val name = varchar("name", 100).uniqueIndex()
    val description = text("description")
    val isSystem = bool("is_system").default(false)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
