package gg.kuken.feature.instance.http.routes

import gg.kuken.feature.instance.InstanceService
import gg.kuken.feature.instance.http.InstanceRoutes
import gg.kuken.http.util.receiveValid
import io.ktor.http.HttpStatusCode
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
private data class CommandRequest(
    @field:NotBlank
    val command: String,
)

fun Route.command() {
    val instanceService by inject<InstanceService>()

    post<InstanceRoutes.Command> { parameters ->
        val request = call.receiveValid<CommandRequest>()
        val exitCode =
            instanceService.runInstanceCommand(
                instanceId = parameters.instanceId,
                commandToRun = request.command,
            )

        call.respond(
            mapOf(
                "exitCode" to exitCode,
            ),
        )
    }
}
