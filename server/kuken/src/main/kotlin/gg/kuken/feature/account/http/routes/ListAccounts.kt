package gg.kuken.feature.account.http.routes

import gg.kuken.feature.account.AccountService
import gg.kuken.feature.account.http.dto.AccountResponse
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject

fun Route.listAccounts() {
    val accountService by inject<AccountService>()

    get<AccountRoutes.List> {
        val accounts = accountService.listAccounts()
        call.respond(accounts.map(::AccountResponse))
    }
}
