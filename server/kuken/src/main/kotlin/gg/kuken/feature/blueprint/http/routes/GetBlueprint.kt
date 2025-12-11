package gg.kuken.feature.blueprint.http.routes

import gg.kuken.feature.blueprint.BlueprintNotFoundException
import gg.kuken.feature.blueprint.BlueprintService
import gg.kuken.feature.blueprint.http.BlueprintRoutes
import gg.kuken.feature.blueprint.http.dto.BlueprintResponse
import gg.kuken.http.HttpError
import gg.kuken.http.util.respondError
import gg.kuken.http.util.validateOrThrow
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import jakarta.validation.Validator
import org.koin.ktor.ext.inject

fun Route.getBlueprint() {
    val blueprintService by inject<BlueprintService>()
    val validator by inject<Validator>()

    get<BlueprintRoutes.ById> { parameters ->
        validator.validateOrThrow(parameters)

        val blueprint =
            try {
                blueprintService.getBlueprint(parameters.blueprintId)
            } catch (_: BlueprintNotFoundException) {
                respondError(HttpError.UnknownBlueprint)
            }

        call.respond(BlueprintResponse(blueprint))
    }
}
