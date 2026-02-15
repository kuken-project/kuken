package gg.kuken.feature.blueprint.http.routes

import gg.kuken.feature.blueprint.http.BlueprintRoutes
import gg.kuken.feature.blueprint.service.BlueprintService
import gg.kuken.http.HttpError
import gg.kuken.http.util.ValidationErrorResponse
import gg.kuken.http.util.ValidationException
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject
import org.pkl.core.PklException

fun Route.resolveBlueprint() {
    val blueprintService by inject<BlueprintService>()

    get<BlueprintRoutes.Resolve> { params ->
        val resolution =
            try {
                blueprintService.resolveBlueprintPartial(params.blueprintId)
            } catch (e: PklException) {
                throw ValidationException(
                    data =
                        ValidationErrorResponse(
                            code = HttpError.BlueprintParse.code,
                            message = e.message.orEmpty(),
                            details = setOf(),
                        ),
                )
            }

        call.respond(resolution)
    }
}
