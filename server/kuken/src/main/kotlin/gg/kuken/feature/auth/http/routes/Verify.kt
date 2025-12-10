package gg.kuken.feature.auth.http.routes

import gg.kuken.feature.account.http.AccountPrincipal
import gg.kuken.feature.auth.http.dto.VerifyResponse
import io.ktor.server.auth.principal
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

fun Route.verify() {
    get<AuthRoutes.Verify> {
        // TODO handle null AccountPrincipal
        val account = call.principal<AccountPrincipal>()!!.account

        call.respond(VerifyResponse(account))
    }
}
