package gg.kuken.feature.unit.http.routes

import gg.kuken.feature.unit.UnitService
import gg.kuken.feature.unit.http.UnitRoutes
import gg.kuken.feature.unit.http.dto.UnitResponse
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject

internal fun Route.listUnits() {
    val unitService by inject<UnitService>()

    get<UnitRoutes.All> {
        val units = unitService.getUnits()
        call.respond(units.map(::UnitResponse))
    }
}
