package gg.kuken.feature.auth.http.routes

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Serializable
@Resource("/auth")
class AuthRoutes {
    @Serializable
    @Resource("login")
    class Login(
        val parent: AuthRoutes = AuthRoutes(),
    )

    @Serializable
    @Resource("")
    class Verify(
        val parent: AuthRoutes = AuthRoutes(),
    )
}
