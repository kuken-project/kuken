package gg.kuken.features.auth.http.routes

import gg.kuken.features.account.http.AccountPrincipal
import gg.kuken.features.auth.http.dto.VerifyResponse
import io.ktor.server.auth.principal
import io.ktor.server.resources.get
import io.ktor.server.routing.Route
import io.ktor.server.response.respond

fun Route.verify() {
    get<AuthRoutes.Verify> {
        // TODO handle null AccountPrincipal
        val account = call.principal<AccountPrincipal>()!!.account

        call.respond(VerifyResponse(account))
    }
}
