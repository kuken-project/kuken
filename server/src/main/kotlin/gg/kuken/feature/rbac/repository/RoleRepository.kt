package gg.kuken.feature.rbac.repository

import gg.kuken.feature.rbac.entity.AccountRolesTable
import gg.kuken.feature.rbac.entity.PermissionResourceRulesTable
import gg.kuken.feature.rbac.entity.PermissionsTable
import gg.kuken.feature.rbac.entity.RolePermissionsTable
import gg.kuken.feature.rbac.entity.RolesTable
import gg.kuken.feature.rbac.model.PermissionPolicy
import gg.kuken.feature.rbac.model.PermissionResourceRule
import gg.kuken.feature.rbac.model.ResourceType
import gg.kuken.feature.rbac.model.Role
import gg.kuken.feature.rbac.model.RolePermission
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class RoleRepository(
    database: Database,
) {
    init {
        transaction(db = database) {
            SchemaUtils.createMissingTablesAndColumns(RolesTable, AccountRolesTable, RolePermissionsTable)
        }
    }

    suspend fun createRole(
        name: String,
        description: String,
        isSystem: Boolean = false,
    ): Role =
        suspendTransaction {
            val id = UUID.randomUUID()
            val now = Clock.System.now()

            RolesTable.insert {
                it[RolesTable.id] = id
                it[RolesTable.name] = name
                it[RolesTable.description] = description
                it[RolesTable.isSystem] = isSystem
                it[createdAt] = now
                it[updatedAt] = now
            }

            Role(
                id = id.toKotlinUuid(),
                name = name,
                description = description,
                isSystem = isSystem,
                createdAt = now,
                updatedAt = now,
            )
        }

    suspend fun getRoleById(id: Uuid): Role? =
        suspendTransaction {
            RolesTable
                .selectAll()
                .where { RolesTable.id eq id.toJavaUuid() }
                .map { it.toRole() }
                .singleOrNull()
        }

    suspend fun addPermissionToRole(
        roleId: Uuid,
        permissionId: Uuid,
        policy: PermissionPolicy = PermissionPolicy.AllowAll,
        allowedResourceIds: List<Uuid> = emptyList(),
        deniedResourceIds: List<Uuid> = emptyList(),
    ): Uuid =
        suspendTransaction {
            val rolePermissionId = UUID.randomUUID()
            val now = Clock.System.now()

            RolePermissionsTable.insert {
                it[RolePermissionsTable.id] = rolePermissionId
                it[RolePermissionsTable.roleId] = roleId.toJavaUuid()
                it[RolePermissionsTable.permissionId] = permissionId.toJavaUuid()
                it[RolePermissionsTable.policy] = policy
                it[grantedAt] = now
            }

            val permission =
                PermissionsTable
                    .selectAll()
                    .where { PermissionsTable.id eq permissionId.toJavaUuid() }
                    .single()
            val resourceType = permission[PermissionsTable.resource]

            when (policy) {
                PermissionPolicy.AllowSpecific -> {
                    allowedResourceIds.forEach { resourceId ->
                        insertPermissionResourceRule(rolePermissionId, resourceType, resourceId, now)
                    }
                }

                PermissionPolicy.DenySpecific -> {
                    deniedResourceIds.forEach { resourceId ->
                        insertPermissionResourceRule(rolePermissionId, resourceType, resourceId, now)
                    }
                }

                PermissionPolicy.AllowAll -> {
                }
            }

            rolePermissionId.toKotlinUuid()
        }

    context(_: Transaction)
    private fun insertPermissionResourceRule(
        rolePermissionId: UUID?,
        resourceType: ResourceType,
        resourceId: Uuid,
        now: Instant,
    ) {
        PermissionResourceRulesTable.insert {
            it[PermissionResourceRulesTable.id] = UUID.randomUUID()
            it[PermissionResourceRulesTable.rolePermissionId] = rolePermissionId
            it[PermissionResourceRulesTable.resourceType] = resourceType
            it[PermissionResourceRulesTable.resourceId] = resourceId.toJavaUuid()
            it[createdAt] = now
        }
    }

    suspend fun getRolePermissions(roleId: Uuid): List<RolePermission> =
        suspendTransaction {
            RolePermissionsTable
                .selectAll()
                .where { RolePermissionsTable.roleId eq roleId.toJavaUuid() }
                .map { it.toRolePermission() }
        }

    suspend fun getResourceRulesForRolePermission(rolePermissionId: Uuid): List<PermissionResourceRule> =
        suspendTransaction {
            PermissionResourceRulesTable
                .selectAll()
                .where { PermissionResourceRulesTable.rolePermissionId eq rolePermissionId.toJavaUuid() }
                .map { it.toResourceRule() }
        }

    suspend fun getAllRoles(): List<Role> =
        suspendTransaction {
            RolesTable.selectAll().map { it.toRole() }
        }

    private fun ResultRow.toRole() =
        Role(
            id = this[RolesTable.id].value.toKotlinUuid(),
            name = this[RolesTable.name],
            description = this[RolesTable.description],
            isSystem = this[RolesTable.isSystem],
            createdAt = this[RolesTable.createdAt],
            updatedAt = this[RolesTable.updatedAt],
        )

    private fun ResultRow.toRolePermission() =
        RolePermission(
            id = this[RolePermissionsTable.id].value.toKotlinUuid(),
            roleId = this[RolePermissionsTable.roleId].value.toKotlinUuid(),
            permissionId = this[RolePermissionsTable.permissionId].value.toKotlinUuid(),
            policy = this[RolePermissionsTable.policy],
            grantedAt = this[RolePermissionsTable.grantedAt],
        )

    private fun ResultRow.toResourceRule() =
        PermissionResourceRule(
            id = this[PermissionResourceRulesTable.id].value.toKotlinUuid(),
            accountPermissionId = this[PermissionResourceRulesTable.accountPermissionId]?.value?.toKotlinUuid(),
            rolePermissionId = this[PermissionResourceRulesTable.rolePermissionId]?.value?.toKotlinUuid(),
            resourceType = this[PermissionResourceRulesTable.resourceType],
            resourceId = this[PermissionResourceRulesTable.resourceId].toKotlinUuid(),
            createdAt = this[PermissionResourceRulesTable.createdAt],
        )
}
