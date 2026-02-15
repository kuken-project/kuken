package gg.kuken.feature.rbac.repository

import gg.kuken.core.ResourceIdFactory
import gg.kuken.feature.rbac.entity.PermissionResourceRulesTable
import gg.kuken.feature.rbac.entity.PermissionsTable
import gg.kuken.feature.rbac.model.Permission
import gg.kuken.feature.rbac.model.PermissionAction
import gg.kuken.feature.rbac.model.ResourceType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Clock
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class PermissionRepository(
    database: Database,
    private val resourceIdFactory: ResourceIdFactory,
) {
    init {
        transaction(db = database) {
            @Suppress("DEPRECATION")
            SchemaUtils.createMissingTablesAndColumns(PermissionsTable, PermissionResourceRulesTable)
        }
    }

    suspend fun createPermission(
        name: String,
        resource: ResourceType,
        action: PermissionAction,
        description: String,
    ): Permission =
        suspendTransaction {
            val id = resourceIdFactory.generate()
            val now = Clock.System.now()

            PermissionsTable.insert {
                it[PermissionsTable.id] = id.value.toJavaUuid()
                it[PermissionsTable.name] = name
                it[PermissionsTable.resource] = resource
                it[PermissionsTable.action] = action
                it[PermissionsTable.description] = description
                it[createdAt] = now
            }

            Permission(
                id = id.value,
                name = name,
                resource = resource,
                action = action,
                description = description,
                createdAt = now,
            )
        }

    suspend fun getPermissionByName(name: String): Permission? =
        suspendTransaction {
            PermissionsTable
                .selectAll()
                .where { PermissionsTable.name eq name }
                .map { it.toPermission() }
                .singleOrNull()
        }

    suspend fun getAllPermissions(): List<Permission> =
        suspendTransaction {
            PermissionsTable.selectAll().map { it.toPermission() }
        }

    private fun ResultRow.toPermission() =
        Permission(
            id = this[PermissionsTable.id].value.toKotlinUuid(),
            name = this[PermissionsTable.name],
            resource = this[PermissionsTable.resource],
            action = this[PermissionsTable.action],
            description = this[PermissionsTable.description],
            createdAt = this[PermissionsTable.createdAt],
        )
}
