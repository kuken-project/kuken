package gg.kuken.feature.instance.http.routes

import gg.kuken.feature.instance.InstanceService
import gg.kuken.feature.instance.http.InstanceRoutes
import gg.kuken.feature.instance.http.dto.RebuildInstanceRequest
import gg.kuken.http.util.receiveValid
import io.ktor.server.resources.patch
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject

fun Route.rebuildInstance() {
    val instanceService by inject<InstanceService>()

    patch<InstanceRoutes.Rebuild> { parameters ->
        val request = call.receiveValid<RebuildInstanceRequest>()
        val instance = instanceService.rebuildInstance(
            instanceId = parameters.instanceId,
            inputs = request.inputs,
            env = request.env,
        )
        call.respond(instance)
    }
}
