package gg.kuken.feature.rbac.service

import gg.kuken.feature.rbac.model.Permission
import gg.kuken.feature.rbac.model.PermissionCheckResult
import gg.kuken.feature.rbac.model.PermissionName
import gg.kuken.feature.rbac.model.PermissionPolicy
import gg.kuken.feature.rbac.model.PermissionSource
import gg.kuken.feature.rbac.repository.AccountPermissionRepository
import gg.kuken.feature.rbac.repository.PermissionRepository
import gg.kuken.feature.rbac.repository.RoleRepository
import kotlin.uuid.Uuid

class PermissionService(
    val permissionRepository: PermissionRepository,
    private val roleRepository: RoleRepository,
    private val accountPermissionRepository: AccountPermissionRepository,
) {
    val emptyPermissionCheckResult =
        PermissionCheckResult(
            hasPermission = false,
            source = null,
            sourceId = null,
            sourceName = null,
            policy = null,
            appliedRule = null,
        )

    suspend fun checkPermission(
        accountId: Uuid,
        permissionName: String,
        resourceId: Uuid? = null,
    ): PermissionCheckResult {
        val permission =
            permissionRepository.getPermissionByName(permissionName)
                ?: return checkWildcardPermission(accountId, permissionName, resourceId)

        val directCheck = checkDirectPermissions(accountId, permission, resourceId)
        if (directCheck.hasPermission) {
            return directCheck
        }

        val roleCheck = checkRolePermissions(accountId, permission, resourceId)
        if (roleCheck.hasPermission) {
            return roleCheck
        }

        if (!permissionName.endsWith("manage")) {
            return checkWildcardPermission(accountId, permissionName, resourceId)
        }

        return emptyPermissionCheckResult
    }

    private suspend fun checkWildcardPermission(
        accountId: Uuid,
        permissionName: String,
        resourceId: Uuid? = null,
    ) = checkPermission(
        accountId = accountId,
        permissionName = permissionName.substringBefore(".") + ".manage",
        resourceId = resourceId,
    )

    suspend fun hasPermission(
        accountId: Uuid,
        permissionName: String,
        resourceId: Uuid? = null,
    ): Boolean = checkPermission(accountId, permissionName, resourceId).hasPermission

    private suspend fun checkDirectPermissions(
        accountId: Uuid,
        permission: Permission,
        resourceId: Uuid?,
    ): PermissionCheckResult {
        val directPermissions = accountPermissionRepository.getAccountDirectPermissions(accountId)

        for (accountPerm in directPermissions) {
            if (accountPerm.permissionId != permission.id) continue

            val isAllowed =
                when (accountPerm.policy) {
                    PermissionPolicy.AllowAll -> {
                        true
                    }

                    PermissionPolicy.AllowSpecific -> {
                        if (resourceId == null) {
                            false
                        } else {
                            val rules = accountPermissionRepository.getResourceRulesForAccountPermission(accountPerm.id)
                            rules.any { it.resourceId == resourceId }
                        }
                    }

                    PermissionPolicy.DenySpecific -> {
                        if (resourceId == null) {
                            true
                        } else {
                            val rules = accountPermissionRepository.getResourceRulesForAccountPermission(accountPerm.id)
                            rules.none { it.resourceId == resourceId }
                        }
                    }
                }

            if (isAllowed) {
                return PermissionCheckResult(
                    hasPermission = true,
                    source = PermissionSource.Direct,
                    sourceId = accountPerm.id,
                    sourceName = "Direct Permission",
                    policy = accountPerm.policy,
                    appliedRule = null,
                )
            }
        }

        return PermissionCheckResult(
            hasPermission = false,
            source = null,
            sourceId = null,
            sourceName = null,
            policy = null,
            appliedRule = null,
        )
    }

    private suspend fun checkRolePermissions(
        accountId: Uuid,
        permission: Permission,
        resourceId: Uuid?,
    ): PermissionCheckResult {
        val accountRoles = accountPermissionRepository.getAccountRoles(accountId)

        for (accountRole in accountRoles) {
            val role = roleRepository.getRoleById(accountRole.roleId) ?: continue
            val rolePermissions = roleRepository.getRolePermissions(accountRole.roleId)

            for (rolePerm in rolePermissions) {
                if (rolePerm.permissionId != permission.id) continue

                val isAllowed =
                    when (rolePerm.policy) {
                        PermissionPolicy.AllowAll -> {
                            true
                        }

                        PermissionPolicy.AllowSpecific -> {
                            if (resourceId == null) {
                                false
                            } else {
                                val rules = roleRepository.getResourceRulesForRolePermission(rolePerm.id)
                                rules.any { it.resourceId == resourceId }
                            }
                        }

                        PermissionPolicy.DenySpecific -> {
                            if (resourceId == null) {
                                true
                            } else {
                                val rules = roleRepository.getResourceRulesForRolePermission(rolePerm.id)
                                rules.none { it.resourceId == resourceId }
                            }
                        }
                    }

                if (isAllowed) {
                    return PermissionCheckResult(
                        hasPermission = true,
                        source = PermissionSource.Role,
                        sourceId = role.id,
                        sourceName = role.name,
                        policy = rolePerm.policy,
                        appliedRule = null,
                    )
                }
            }
        }

        return PermissionCheckResult(
            hasPermission = false,
            source = null,
            sourceId = null,
            sourceName = null,
            policy = null,
            appliedRule = null,
        )
    }

    suspend fun getEffectivePermissions(accountId: Uuid): List<PermissionName> {
        val effectivePermissions = mutableListOf<PermissionName>()
        val allPermissions = permissionRepository.getAllPermissions()

        for (permission in allPermissions) {
            val check = checkPermission(accountId, permission.name, null)
            if (check.hasPermission) {
                effectivePermissions.add(permission.name)
            }
        }

        return effectivePermissions
    }

    suspend fun getEffectivePermissionsWithSource(accountId: Uuid): List<Pair<Permission, PermissionCheckResult>> {
        val effectivePermissions = mutableListOf<Pair<Permission, PermissionCheckResult>>()
        val allPermissions = permissionRepository.getAllPermissions()

        for (permission in allPermissions) {
            val check = checkPermission(accountId, permission.name, null)
            if (check.hasPermission) {
                effectivePermissions.add(permission to check)
            }
        }

        return effectivePermissions
    }

    suspend fun getEffectivePermissionsForResource(
        accountId: Uuid,
        resourceId: Uuid,
    ): List<Pair<Permission, PermissionCheckResult>> {
        val effectivePermissions = mutableListOf<Pair<Permission, PermissionCheckResult>>()
        val allPermissions = permissionRepository.getAllPermissions()

        for (permission in allPermissions) {
            val check = checkPermission(accountId, permission.name, resourceId)
            if (check.hasPermission) {
                effectivePermissions.add(permission to check)
            }
        }

        return effectivePermissions
    }
}
