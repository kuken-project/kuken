package gg.kuken.feature.auth.http.routes

import gg.kuken.feature.auth.http.dto.VerifyResponse
import gg.kuken.feature.rbac.http.getCurrentAccount
import gg.kuken.feature.rbac.service.PermissionService
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject

fun Route.verify() {
    val permissionService by inject<PermissionService>()

    get<AuthRoutes.Verify> {
        val account = call.getCurrentAccount()
        val permissions = permissionService.getEffectivePermissions(account.id)

        call.respond(
            VerifyResponse(
                id = account.id.toString(),
                email = account.email,
                createdAt = account.createdAt,
                updatedAt = account.updatedAt,
                lastLoggedInAt = account.lastLoggedInAt,
                permissions = permissions,
            ),
        )
    }
}
