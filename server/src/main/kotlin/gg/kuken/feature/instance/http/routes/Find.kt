package gg.kuken.feature.instance.http.routes

import gg.kuken.feature.instance.InstanceService
import gg.kuken.feature.instance.http.InstanceRoutes
import gg.kuken.http.util.validateOrThrow
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import jakarta.validation.Validator
import org.koin.ktor.ext.inject

fun Route.getInstanceDetails() {
    val instanceService by inject<InstanceService>()
    val validator by inject<Validator>()

    get<InstanceRoutes.ById> { parameters ->
        validator.validateOrThrow(parameters)

        val instance = instanceService.getInstance(parameters.instanceId)
        call.respond(instance)
    }
}
