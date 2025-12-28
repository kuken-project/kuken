package gg.kuken.feature.rbac.http

import gg.kuken.feature.account.http.AccountPrincipal
import gg.kuken.feature.auth.http.exception.InvalidAccessTokenException
import gg.kuken.feature.rbac.Permissions
import gg.kuken.feature.rbac.exception.InsufficientPermissionsException
import gg.kuken.feature.rbac.model.PermissionCheckResult
import gg.kuken.feature.rbac.model.PermissionName
import gg.kuken.feature.rbac.service.PermissionService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.koin.ktor.ext.inject
import kotlin.uuid.Uuid

fun ApplicationCall.getAccountId(): Uuid =
    principal<AccountPrincipal>()
        ?.account
        ?.id
        ?: throw InvalidAccessTokenException()

suspend fun ApplicationCall.isAdmin(): Boolean {
    val userId = getAccountId()
    val permissionService by inject<PermissionService>()
    return permissionService.hasPermission(userId, Permissions.Admin)
}

typealias GetResourceId = (ApplicationCall) -> Uuid?

class SinglePermissionRoutePluginConfig {
    lateinit var permissions: List<PermissionName>
    var getResourceId: GetResourceId? = null
}

private val PermissionPhase = PipelinePhase("permission")

private object PermissionRouteHook : Hook<suspend (ApplicationCall) -> Any> {
    override fun install(
        pipeline: ApplicationCallPipeline,
        handler: suspend (ApplicationCall) -> Any,
    ) {
        // Needed to ensure route-scoped permissions plugin runs after Authentication plugin
        pipeline.insertPhaseAfter(ApplicationCallPipeline.Plugins, PermissionPhase)
        pipeline.intercept(PermissionPhase) {
            handler(call)
            proceedWith(Unit)
        }
    }
}

private val PermissionRoutePlugin =
    createRouteScopedPlugin(
        name = "permission",
        createConfiguration = ::SinglePermissionRoutePluginConfig,
    ) {
        on(PermissionRouteHook) { call ->
            val permissionService by call.inject<PermissionService>()
            val accountId = call.getAccountId()
            val resourceId = pluginConfig.getResourceId?.invoke(call)

            val hasPermission: Boolean

            if (pluginConfig.permissions.size > 1) {
                hasPermission =
                    coroutineScope {
                        val deferredList =
                            pluginConfig.permissions.map { permission ->
                                async {
                                    permissionService.hasPermission(
                                        accountId = accountId,
                                        permissionName = permission,
                                        resourceId = resourceId,
                                    )
                                }
                            }

                        deferredList.awaitAll().any {
                            it // is allowed
                        }
                    }
            } else {
                hasPermission =
                    permissionService.hasPermission(
                        accountId = accountId,
                        permissionName = pluginConfig.permissions.first(),
                        resourceId = resourceId,
                    )
            }

            if (!hasPermission) {
                throw InsufficientPermissionsException()
            }
        }
    }

fun Route.requirePermission(
    vararg permission: PermissionName,
    getResourceId: GetResourceId? = null,
) {
    install(PermissionRoutePlugin) {
        this.permissions = permission.toList()
        this.getResourceId = getResourceId
    }
}

suspend fun ApplicationCall.checkPermissionWithDetails(
    permissionName: String,
    resourceId: Uuid? = null,
): PermissionCheckResult {
    val permissionService by inject<PermissionService>()
    val getUserId = application.attributes[GetUserIdKey]

    val userId =
        getUserId(this) ?: return PermissionCheckResult(
            hasPermission = false,
            source = null,
            sourceId = null,
            sourceName = null,
            policy = null,
            appliedRule = null,
        )

    return permissionService.checkPermission(userId, permissionName, resourceId)
}
