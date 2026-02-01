package gg.kuken.feature.setup

import gg.kuken.feature.account.AccountService
import gg.kuken.feature.blueprint.BlueprintService
import gg.kuken.feature.blueprint.BlueprintSpecSource
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
import gg.kuken.feature.remoteConfig.RemoteConfig
import gg.kuken.feature.remoteConfig.RemoteConfigService
import gg.kuken.feature.setup.http.dto.SetupRequest
import gg.kuken.feature.setup.model.SetupState
import gg.kuken.feature.setup.model.SetupState.Step
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement

class SetupService(
    private val accountService: AccountService,
    private val remoteConfigService: RemoteConfigService,
    private val accountPermissionRepository: AccountPermissionRepository,
    private val permissionRepository: PermissionRepository,
    private val roleRepository: RoleRepository,
    private val blueprintService: BlueprintService,
) {
    val requiredSetupSteps = linkedSetOf(Step.OrganizationName, Step.CreateAccount)

    suspend fun currentState(): SetupState {
        val stepsToComplete = retrieveRemainingSteps()

        return SetupState(
            completed = stepsToComplete.isEmpty(),
            remainingSteps =
                requiredSetupSteps
                    .mapNotNull { step -> stepsToComplete.firstOrNull { it == step } }
                    .filter { step -> step in requiredSetupSteps }
                    .toSet(),
        )
    }

    suspend fun tryComplete(request: SetupRequest): SetupState =
        coroutineScope {
            val setConfigValue =
                async {
                    remoteConfigService.setConfigValue(
                        key = RemoteConfig.OrganizationName,
                        value = request.organizationName,
                    )
                }

            val firstAccount =
                async {
                    val adminRole = setupDefaultRoles()
                    createFirstAccount(
                        email = request.account.email,
                        password = request.account.password,
                        role = adminRole,
                    )
                }

            val blueprints = async { importBlueprints() }

            listOf(
                setConfigValue,
                firstAccount,
                blueprints,
            ).awaitAll()

            SetupState(
                completed = true,
                remainingSteps = emptySet(),
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

    suspend fun setupDefaultRoles(): Role {
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
                    isSystem = true,
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

        return adminRole
    }

    suspend fun createFirstAccount(
        email: String,
        password: String,
        role: Role,
    ) {
        val account = accountService.createAccount(email, password)
        accountPermissionRepository.assignRoleToAccount(
            accountId = account.id,
            roleId = role.id,
            grantedBy = account.id,
            expiresAt = null,
        )
    }

    suspend fun importBlueprints() =
        coroutineScope {
            val baseRemoteUrl =
                "https://raw.githubusercontent.com/kuken-project/blueprints/refs/heads/main/blueprints/games"
            listOf("minecraft/minecraft-java-edition", "hytale/hytale")
                .map { baseName ->
                    async {
                        val source: BlueprintSpecSource =
                            Json.decodeFromJsonElement(JsonPrimitive("$baseRemoteUrl/$baseName.pkl"))
                        blueprintService.importBlueprint(source)
                    }
                }.awaitAll()
        }
}
