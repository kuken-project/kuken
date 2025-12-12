package gg.kuken.feature.unit.http.routes

import gg.kuken.feature.unit.UnitNotFoundException
import gg.kuken.feature.unit.UnitService
import gg.kuken.feature.unit.http.UnitRoutes
import gg.kuken.feature.unit.http.dto.UnitResponse
import gg.kuken.http.HttpError
import gg.kuken.http.util.respondError
import gg.kuken.http.util.validateOrThrow
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import jakarta.validation.Validator
import org.koin.ktor.ext.inject

internal fun Route.getUnit() {
    val unitService by inject<UnitService>()
    val validator by inject<Validator>()

    get<UnitRoutes.ById> { parameters ->
        validator.validateOrThrow(parameters)

        val unit =
            try {
                unitService.getUnit(parameters.unitId)
            } catch (_: UnitNotFoundException) {
                respondError(HttpError.UnknownUnit)
            }

        call.respond(UnitResponse(unit))
    }
}
