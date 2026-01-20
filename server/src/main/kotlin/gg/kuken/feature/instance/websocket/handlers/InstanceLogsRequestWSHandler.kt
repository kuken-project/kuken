package gg.kuken.feature.instance.websocket.handlers

import gg.kuken.feature.instance.InstanceService
import gg.kuken.feature.instance.http.getInstance
import gg.kuken.feature.instance.model.ConsoleLogFrame
import gg.kuken.feature.instance.websocket.InstanceLogsConsoleSession
import gg.kuken.feature.instance.websocket.InstanceLogsConsoleSessionAttributeKey
import gg.kuken.websocket.WebSocketClientMessageContext
import gg.kuken.websocket.WebSocketClientMessageHandler
import gg.kuken.websocket.WebSocketOpCodes
import gg.kuken.websocket.long
import gg.kuken.websocket.respond
import gg.kuken.websocket.respondAsync
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import me.devnatan.dockerkt.DockerClient
import me.devnatan.dockerkt.models.container.ContainerLogsOptions
import me.devnatan.dockerkt.models.container.ContainerLogsResult
import me.devnatan.dockerkt.resource.container.ContainerNotFoundException

class InstanceLogsRequestWSHandler(
    val instanceService: InstanceService,
    val dockerClient: DockerClient,
) : WebSocketClientMessageHandler() {
    override suspend fun WebSocketClientMessageContext.handle() {
        val instance = with(instanceService) { packet.getInstance() }
        val since = packet.long("since")

        val containerId = instance.containerId
        if (containerId == null) {
            respond(WebSocketOpCodes.InstanceUnavailable)
            return
        }

        val consoleSession =
            session.connection.call.attributes.computeIfAbsent(InstanceLogsConsoleSessionAttributeKey) {
                InstanceLogsConsoleSession(instance.id.toString())
            }

        try {
            captureLogs(containerId, since) { frame ->
                val frame = consoleSession.addLog(frame)
                respondAsync(
                    op = WebSocketOpCodes.InstanceLogsRequestFrame,
                    data = frame,
                )
            }
        } catch (_: ContainerNotFoundException) {
            respond(WebSocketOpCodes.InstanceUnavailable)
        } finally {
            session.connection.call.attributes
                .remove(InstanceLogsConsoleSessionAttributeKey)
        }
    }

    private suspend fun captureLogs(
        containerId: String,
        since: Long,
        onFrame: (ConsoleLogFrame) -> Unit,
    ) {
        val options =
            ContainerLogsOptions(
                follow = true,
                stderr = true,
                stdout = true,
                demux = false,
                showTimestamps = true,
            )
        val logs =
            dockerClient.containers.logs(
                container = containerId,
                options = options,
            ) as ContainerLogsResult.Stream

        try {
            logs.output
                .map { frame -> ConsoleLogFrame.fromText(frame.value.removeSuffix("\n"), frame.stream) }
                .let { flow ->
                    if (since > 0) {
                        flow.filter { frame -> frame.timestamp >= since }
                    } else {
                        flow
                    }
                }.collect { frame -> onFrame(frame) }
        } catch (_: CancellationException) {
            // Session closed while fetching logs
        }
    }
}
