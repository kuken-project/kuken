package gg.kuken.feature.account.http.routes

import gg.kuken.feature.account.AccountService
import gg.kuken.feature.account.http.dto.RegisterRequest
import gg.kuken.feature.account.http.dto.RegisterResponse
import gg.kuken.http.util.receiveValidating
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import jakarta.validation.Validator
import org.koin.ktor.ext.inject

fun Route.register() {
    val accountService by inject<AccountService>()
    val validator by inject<Validator>()

    post<AccountRoutes.Register> {
        val payload = call.receiveValidating<RegisterRequest>(validator)
        val account =
            accountService.createAccount(
                email = payload.email,
                password = payload.password,
            )

        call.respond(RegisterResponse(account))
    }
}
