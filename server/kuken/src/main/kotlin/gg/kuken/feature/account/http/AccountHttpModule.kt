package gg.kuken.feature.account.http

import gg.kuken.feature.account.http.routes.listAccounts
import gg.kuken.feature.account.http.routes.register
import gg.kuken.http.HttpModule
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.routing

object AccountHttpModule : HttpModule() {
    override fun install(app: Application) {
        app.routing {
            authenticate {
                listAccounts()
            }
            register()
        }
    }
}
