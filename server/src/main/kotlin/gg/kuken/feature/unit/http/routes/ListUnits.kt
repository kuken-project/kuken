package gg.kuken.feature.unit.http.routes

import gg.kuken.feature.unit.UnitService
import gg.kuken.feature.unit.http.UnitRoutes
import gg.kuken.feature.unit.http.mapper.UnitMapper
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject
import org.koin.ktor.ext.get as koinGet

internal fun Route.listUnits() {
    val unitService by inject<UnitService>()

    get<UnitRoutes.All> {
        val units = unitService.getUnits()
        val mapper = koinGet<UnitMapper>()

        call.respond(units.map { mapper.invoke(it) })
    }
}
