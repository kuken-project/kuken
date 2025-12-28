package gg.kuken.feature.setup

import gg.kuken.feature.account.AccountService
import gg.kuken.feature.rbac.Permissions
import gg.kuken.feature.rbac.Permissions.ManageAccounts
import gg.kuken.feature.rbac.Permissions.ManageBlueprints
import gg.kuken.feature.rbac.Permissions.ManageInstances
import gg.kuken.feature.rbac.Permissions.ManageRoles
import gg.kuken.feature.rbac.Permissions.ManageUnits
import gg.kuken.feature.rbac.model.PermissionAction
import gg.kuken.feature.rbac.model.PermissionPolicy
import gg.kuken.feature.rbac.model.ResourceType
import gg.kuken.feature.rbac.model.Role
import gg.kuken.feature.rbac.repository.AccountPermissionRepository
import gg.kuken.feature.rbac.repository.PermissionRepository
import gg.kuken.feature.rbac.repository.RoleRepository
import gg.kuken.feature.rbac.service.PermissionService
import gg.kuken.feature.remoteConfig.RemoteConfig
import gg.kuken.feature.remoteConfig.RemoteConfigService
import gg.kuken.feature.setup.http.dto.SetupRequest
import gg.kuken.feature.setup.model.SetupState
import gg.kuken.feature.setup.model.SetupState.Step
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class SetupService(
    private val accountService: AccountService,
    private val remoteConfigService: RemoteConfigService,
    private val accountPermissionRepository: AccountPermissionRepository,
    private val permissionRepository: PermissionRepository,
    private val roleRepository: RoleRepository,
) {
    val requiredSetupSteps = linkedSetOf(Step.CreateAccount, Step.OrganizationName)

    suspend fun currentState(): SetupState {
        val stepsToComplete =
            coroutineScope {
                retrieveRemainingSteps()
            }

        return SetupState(
            completed = stepsToComplete.isEmpty(),
            remainingSteps = stepsToComplete.filter { step -> step in requiredSetupSteps }.toSet(),
        )
    }

    private suspend fun retrieveRemainingSteps(): Set<Step> =
        buildSet {
            if (!accountService.existsAnyAccount()) {
                add(Step.CreateAccount)
            }

            if (!remoteConfigService.isConfigValueSet(RemoteConfig.OrganizationName)) {
                add(Step.OrganizationName)
            }
        }

    suspend fun tryComplete(request: SetupRequest): SetupState {
        coroutineScope {
            remoteConfigService.setConfigValue(
                key = RemoteConfig.OrganizationName,
                value = request.organizationName,
            )

            val adminRole = setupDefaultRoles()
            createFirstAccount(
                email = request.account.email,
                password = request.account.password,
                role = adminRole,
            )
        }

        return SetupState(
            completed = true,
            remainingSteps = emptySet(),
        )
    }

    suspend fun setupDefaultRoles(): Role =
        coroutineScope {
            // Core permissions that the Administrator role needs
            val permissions =
                listOf(
                    ManageAccounts to ResourceType.Account,
                    ManageRoles to ResourceType.Role,
                    ManageUnits to ResourceType.Unit,
                    ManageInstances to ResourceType.Instance,
                    ManageBlueprints to ResourceType.Blueprint,
                )

            val adminRole =
                withContext(IO) {
                    roleRepository.createRole(
                        name = "Administrator",
                        description = "Access to all resources",
                        isSystem = true, // cannot be deleted or modified by regular users
                    )
                }

            val createdPermissions =
                withContext(IO) {
                    permissions
                        .map { (permission, resourceType) ->
                            async {
                                permissionRepository.createPermission(
                                    name = permission,
                                    resource = resourceType,
                                    action = PermissionAction.Manage,
                                    description = "Manage any ${resourceType.name.lowercase()}",
                                )
                            }
                        }.awaitAll()
                }

            // Attach all created permissions to the Administrator role.
            // Using AllowAll policy grants unrestricted access to all resources
            // of each permission's type without requiring specific resource IDs.
            withContext(IO) {
                createdPermissions
                    .map { permission ->
                        async {
                            roleRepository.addPermissionToRole(
                                roleId = adminRole.id,
                                permissionId = permission.id,
                                policy = PermissionPolicy.AllowAll,
                            )
                        }
                    }.awaitAll()
            }

            adminRole
        }

    suspend fun createFirstAccount(
        email: String,
        password: String,
        role: Role,
    ) = coroutineScope {
        val account = accountService.createAccount(email, password)

        accountPermissionRepository.assignRoleToAccount(
            accountId = account.id,
            roleId = role.id,
            grantedBy = account.id,
            expiresAt = null,
        )
    }
}
