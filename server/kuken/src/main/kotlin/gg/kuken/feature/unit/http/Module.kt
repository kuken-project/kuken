package gg.kuken.feature.unit.http

import gg.kuken.feature.unit.http.routes.createUnit
import gg.kuken.feature.unit.http.routes.getUnit
import gg.kuken.feature.unit.http.routes.listUnits
import gg.kuken.http.HttpModule
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.routing

internal object UnitHttpModule : HttpModule() {
    override fun install(app: Application) {
        app.routing {
            authenticate {
                listUnits()
                getUnit()
                createUnit()
            }
        }
    }
}
