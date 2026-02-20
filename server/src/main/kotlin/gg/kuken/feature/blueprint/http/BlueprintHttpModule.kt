package gg.kuken.feature.blueprint.http

import gg.kuken.feature.blueprint.http.routes.getBlueprint
import gg.kuken.feature.blueprint.http.routes.importBlueprint
import gg.kuken.feature.blueprint.http.routes.listBlueprints
import gg.kuken.feature.blueprint.http.routes.resolveBlueprint
import gg.kuken.feature.rbac.Permissions
import gg.kuken.feature.rbac.http.withPermission
import gg.kuken.http.HttpModule
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.routing

object BlueprintHttpModule : HttpModule() {
    override fun install(app: Application): Unit =
        with(app) {
            routing {
                authenticate {
                    withPermission(Permissions.ManageBlueprints) {
                        listBlueprints()
                        getBlueprint()
                    }

                    withPermission(Permissions.ImportBlueprints) {
                        importBlueprint()
                    }

                    resolveBlueprint()
                }
            }
        }
}
