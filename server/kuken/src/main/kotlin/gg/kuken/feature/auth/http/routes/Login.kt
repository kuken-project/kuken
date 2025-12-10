package gg.kuken.feature.auth.http.routes

import gg.kuken.feature.auth.AuthService
import gg.kuken.feature.auth.http.dto.LoginRequest
import gg.kuken.feature.auth.http.dto.LoginResponse
import gg.kuken.http.util.receiveValid
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject

fun Route.login() {
    val authService by inject<AuthService>()

    post<AuthRoutes.Login> {
        val req = call.receiveValid<LoginRequest>()
        val token =
            authService.auth(
                username = req.username,
                password = req.password,
            )

        call.respond(LoginResponse(token))
    }
}
