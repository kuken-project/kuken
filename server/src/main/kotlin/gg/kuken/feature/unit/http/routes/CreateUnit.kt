package gg.kuken.feature.unit.http.routes

import gg.kuken.feature.account.http.AccountKey
import gg.kuken.feature.unit.UnitService
import gg.kuken.feature.unit.http.UnitRoutes
import gg.kuken.feature.unit.http.dto.CreateUnitRequest
import gg.kuken.feature.unit.http.mapper.UnitMapper
import gg.kuken.feature.unit.model.UnitCreateOptions
import gg.kuken.http.util.receiveValidating
import io.ktor.http.HttpStatusCode
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import jakarta.validation.Validator
import org.koin.ktor.ext.get
import org.koin.ktor.ext.inject

internal fun Route.createUnit() {
    val unitService by inject<UnitService>()
    val validator by inject<Validator>()

    post<UnitRoutes> {
        val request = call.receiveValidating<CreateUnitRequest>(validator)
        val unit = unitService.createUnit(buildUnitCreateOptions(request))

        val mapper = get<UnitMapper>()
        call.respond(
            message = mapper.invoke(unit),
            status = HttpStatusCode.Created,
        )
    }
}

private fun RoutingContext.buildUnitCreateOptions(request: CreateUnitRequest): UnitCreateOptions =
    UnitCreateOptions(
        name = request.name,
        blueprintId = request.blueprint,
        externalId = null,
        actorId = call.attributes.getOrNull(AccountKey)?.id,
        image = request.image,
        options = request.options,
        network =
            request.network?.let { network ->
                UnitCreateOptions.Network(
                    host = network.host,
                    port = network.port,
                )
            },
    )
