package gg.kuken.feature.blueprint.http.routes

import gg.kuken.feature.blueprint.BlueprintService
import gg.kuken.feature.blueprint.http.BlueprintRoutes
import gg.kuken.feature.blueprint.http.dto.BlueprintResponse
import gg.kuken.feature.blueprint.http.dto.ListBlueprintsResponse
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject

fun Route.listBlueprints() {
    val blueprintService by inject<BlueprintService>()

    get<BlueprintRoutes.All> {
        val blueprints = blueprintService.listBlueprints()

        call.respond(
            ListBlueprintsResponse(
                blueprints.map(::BlueprintResponse),
            ),
        )
    }
}
