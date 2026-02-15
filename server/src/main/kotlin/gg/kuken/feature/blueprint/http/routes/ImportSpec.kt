package gg.kuken.feature.blueprint.http.routes

import gg.kuken.feature.blueprint.BlueprintSpecSource
import gg.kuken.feature.blueprint.http.BlueprintRoutes
import gg.kuken.feature.blueprint.service.BlueprintService
import gg.kuken.http.util.receiveValid
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class ImportBlueprintRequest(
    val source: BlueprintSpecSource,
)

fun Route.importBlueprint() {
    val blueprintService by inject<BlueprintService>()

    post<BlueprintRoutes.Import> {
        val payload = call.receiveValid<ImportBlueprintRequest>()
        val blueprint = blueprintService.importBlueprint(payload.source)

        call.respond(blueprint)
    }
}
