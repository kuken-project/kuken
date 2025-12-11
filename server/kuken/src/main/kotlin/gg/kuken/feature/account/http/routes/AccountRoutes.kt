package gg.kuken.feature.account.http.routes

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Serializable
@Resource("/accounts")
class AccountRoutes {
    @Serializable
    @Resource("")
    class List(
        val parent: AccountRoutes = AccountRoutes(),
    )

    @Serializable
    @Resource("")
    class Register(
        val parent: AccountRoutes = AccountRoutes(),
    )
}
