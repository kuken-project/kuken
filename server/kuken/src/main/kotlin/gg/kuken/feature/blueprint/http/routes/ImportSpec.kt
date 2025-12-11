package gg.kuken.feature.blueprint.http.routes

import gg.kuken.feature.blueprint.BlueprintService
import gg.kuken.feature.blueprint.RemoteBlueprintSpecSource
import gg.kuken.feature.blueprint.http.BlueprintRoutes
import gg.kuken.feature.blueprint.http.dto.ImportBlueprintRequest
import gg.kuken.http.util.receiveValidating
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import jakarta.validation.Validator
import org.koin.ktor.ext.inject

fun Route.importBlueprint() {
    val blueprintService by inject<BlueprintService>()
    val validator by inject<Validator>()

    post<BlueprintRoutes.Import> {
        val req = call.receiveValidating<ImportBlueprintRequest>(validator)
        val spec = blueprintService.importBlueprint(RemoteBlueprintSpecSource(req.url))

        call.respond(spec)
    }
}
