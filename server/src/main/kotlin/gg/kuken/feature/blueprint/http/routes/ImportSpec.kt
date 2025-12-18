package gg.kuken.feature.blueprint.http.routes

import gg.kuken.feature.blueprint.BlueprintService
import gg.kuken.feature.blueprint.BlueprintSpecSource
import gg.kuken.feature.blueprint.http.BlueprintRoutes
import gg.kuken.http.util.receiveValid
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject

fun Route.importBlueprint() {
    val blueprintService by inject<BlueprintService>()

    post<BlueprintRoutes.Import> {
        val source = call.receiveValid<BlueprintSpecSource>()
        val blueprint = blueprintService.importBlueprint(source)

        call.respond(blueprint)
    }
}
