package gg.kuken.feature.instance.http.routes

import gg.kuken.feature.instance.InstanceService
import gg.kuken.feature.instance.http.InstanceRoutes
import gg.kuken.feature.instance.http.dto.ExecuteCommandRequest
import gg.kuken.feature.instance.http.dto.ExecuteCommandResponse
import gg.kuken.http.util.receiveValid
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject

fun Route.command() {
    val instanceService by inject<InstanceService>()

    post<InstanceRoutes.Command> { parameters ->
        val request = call.receiveValid<ExecuteCommandRequest>()
        val exitCode =
            instanceService.runInstanceCommand(
                instanceId = parameters.instanceId,
                commandToRun = request.command,
            )

        call.respond(ExecuteCommandResponse(exitCode))
    }
}
