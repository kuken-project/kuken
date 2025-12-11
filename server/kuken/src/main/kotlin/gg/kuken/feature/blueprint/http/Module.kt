package gg.kuken.feature.blueprint.http

import gg.kuken.feature.blueprint.http.routes.getBlueprint
import gg.kuken.feature.blueprint.http.routes.importBlueprint
import gg.kuken.feature.blueprint.http.routes.listBlueprints
import gg.kuken.http.HttpModule
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

object BlueprintHttpModule : HttpModule() {
    override fun install(app: Application): Unit =
        with(app) {
            routing {
                authenticate {
                    getBlueprint()
                    listBlueprints()
                    importBlueprint()
                }
            }
        }
}
