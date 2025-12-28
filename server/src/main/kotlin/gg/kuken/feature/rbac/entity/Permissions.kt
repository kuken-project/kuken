package gg.kuken.feature.rbac.entity

import gg.kuken.feature.rbac.model.PermissionAction
import gg.kuken.feature.rbac.model.ResourceType
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp

object PermissionsTable : UUIDTable("permissions") {
    val name = varchar("name", 255).uniqueIndex()
    val resource = enumerationByName<ResourceType>("resource", 50)
    val action = enumerationByName<PermissionAction>("action", 50)
    val description = text("description")
    val createdAt = timestamp("created_at")
}
