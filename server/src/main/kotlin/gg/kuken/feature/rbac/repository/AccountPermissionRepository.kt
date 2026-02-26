package gg.kuken.feature.rbac.repository

import gg.kuken.feature.account.model.AccountPermission
import gg.kuken.feature.account.model.AccountRole
import gg.kuken.feature.rbac.entity.AccountPermissionsTable
import gg.kuken.feature.rbac.entity.AccountRolesTable
import gg.kuken.feature.rbac.entity.PermissionResourceRulesTable
import gg.kuken.feature.rbac.entity.PermissionsTable
import gg.kuken.feature.rbac.model.PermissionPolicy
import gg.kuken.feature.rbac.model.PermissionResourceRule
import gg.kuken.feature.rbac.model.ResourceType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
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

class AccountPermissionRepository(
    database: Database,
) {
    init {
        transaction(db = database) {
            @Suppress("DEPRECATION")
            SchemaUtils.createMissingTablesAndColumns(AccountPermissionsTable)
        }
    }

    suspend fun assignRoleToAccount(
        accountId: Uuid,
        roleId: Uuid,
        grantedBy: Uuid,
        expiresAt: Instant?,
    ): AccountRole =
        suspendTransaction {
            val id = UUID.randomUUID()
            val now = Clock.System.now()

            AccountRolesTable.insert {
                it[AccountRolesTable.id] = id
                it[AccountRolesTable.accountId] = accountId.toJavaUuid()
                it[AccountRolesTable.roleId] = roleId.toJavaUuid()
                it[grantedAt] = now
                it[AccountRolesTable.grantedBy] = grantedBy.toJavaUuid()
                it[AccountRolesTable.expiresAt] = expiresAt
            }

            AccountRole(
                id = id.toKotlinUuid(),
                accountId = accountId,
                roleId = roleId,
                grantedAt = now,
                grantedBy = grantedBy,
                expiresAt = expiresAt,
            )
        }

    suspend fun grantDirectPermission(
        accountId: Uuid,
        permissionId: Uuid,
        policy: PermissionPolicy = PermissionPolicy.AllowAll,
        allowedResourceIds: List<UUID> = emptyList(),
        deniedResourceIds: List<UUID> = emptyList(),
        expiresAt: Instant? = null,
    ): Uuid =
        suspendTransaction {
            val accountPermissionId = UUID.randomUUID()
            val now = Clock.System.now()

            AccountPermissionsTable.insert {
                it[AccountPermissionsTable.id] = accountPermissionId
                it[AccountPermissionsTable.accountId] = accountId.toJavaUuid()
                it[AccountPermissionsTable.permissionId] = permissionId.toJavaUuid()
                it[AccountPermissionsTable.policy] = policy
                it[grantedAt] = now
                it[AccountPermissionsTable.expiresAt] = expiresAt
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
                        insertPermissionResourceRule(accountPermissionId, resourceType, resourceId, now)
                    }
                }

                PermissionPolicy.DenySpecific -> {
                    deniedResourceIds.forEach { resourceId ->
                        insertPermissionResourceRule(accountPermissionId, resourceType, resourceId, now)
                    }
                }

                PermissionPolicy.AllowAll -> {
                }
            }

            accountPermissionId.toKotlinUuid()
        }

    context(_: Transaction)
    private fun insertPermissionResourceRule(
        accountPermissionId: UUID?,
        resourceType: ResourceType,
        resourceId: UUID,
        now: Instant,
    ) {
        PermissionResourceRulesTable.insert {
            it[PermissionResourceRulesTable.id] = UUID.randomUUID()
            it[PermissionResourceRulesTable.accountPermissionId] = accountPermissionId
            it[PermissionResourceRulesTable.resourceType] = resourceType
            it[PermissionResourceRulesTable.resourceId] = resourceId
            it[createdAt] = now
        }
    }

    suspend fun getAccountRoles(accountId: Uuid): List<AccountRole> =
        suspendTransaction {
            val now = Clock.System.now()

            AccountRolesTable
                .selectAll()
                .where {
                    (AccountRolesTable.accountId eq accountId.toJavaUuid()) and
                        (AccountRolesTable.expiresAt.isNull() or (AccountRolesTable.expiresAt greater now))
                }.map { it.toAccountRole() }
        }

    suspend fun getAccountDirectPermissions(accountId: Uuid): List<AccountPermission> =
        suspendTransaction {
            val now = Clock.System.now()

            AccountPermissionsTable
                .selectAll()
                .where {
                    (AccountPermissionsTable.accountId eq accountId.toJavaUuid()) and
                        (AccountPermissionsTable.expiresAt.isNull() or (AccountPermissionsTable.expiresAt greater now))
                }.map { it.toAccountPermission() }
        }

    suspend fun getResourceRulesForAccountPermission(accountPermissionId: Uuid): List<PermissionResourceRule> =
        suspendTransaction {
            PermissionResourceRulesTable
                .selectAll()
                .where { PermissionResourceRulesTable.accountPermissionId eq accountPermissionId.toJavaUuid() }
                .map { it.toResourceRule() }
        }

    suspend fun revokeRoleFromAccount(
        accountId: Uuid,
        roleId: Uuid,
    ) = suspendTransaction {
        AccountRolesTable.deleteWhere {
            (AccountRolesTable.accountId eq accountId.toJavaUuid()) and
                (AccountRolesTable.roleId eq roleId.toJavaUuid())
        }
    }

    suspend fun revokeDirectPermission(
        accountId: Uuid,
        permissionId: Uuid,
    ) = suspendTransaction {
        AccountPermissionsTable.deleteWhere {
            (AccountPermissionsTable.accountId eq accountId.toJavaUuid()) and
                (AccountPermissionsTable.permissionId eq permissionId.toJavaUuid())
        }
    }

    private fun ResultRow.toAccountRole() =
        AccountRole(
            id = this[AccountRolesTable.id].value.toKotlinUuid(),
            accountId = this[AccountRolesTable.accountId].toKotlinUuid(),
            roleId = this[AccountRolesTable.roleId].value.toKotlinUuid(),
            grantedAt = this[AccountRolesTable.grantedAt],
            grantedBy = this[AccountRolesTable.grantedBy].toKotlinUuid(),
            expiresAt = this[AccountRolesTable.expiresAt],
        )

    private fun ResultRow.toAccountPermission() =
        AccountPermission(
            id = this[AccountPermissionsTable.id].value.toKotlinUuid(),
            accountId = this[AccountPermissionsTable.accountId].toKotlinUuid(),
            permissionId = this[AccountPermissionsTable.permissionId].value.toKotlinUuid(),
            policy = this[AccountPermissionsTable.policy],
            grantedAt = this[AccountPermissionsTable.grantedAt],
            expiresAt = this[AccountPermissionsTable.expiresAt],
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
