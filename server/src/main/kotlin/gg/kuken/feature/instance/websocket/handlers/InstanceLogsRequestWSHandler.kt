package gg.kuken.feature.instance.websocket.handlers

import gg.kuken.core.ResourceId
import gg.kuken.feature.instance.ActivityLogStore
import gg.kuken.feature.instance.InstanceNotFoundException
import gg.kuken.feature.instance.InstanceService
import gg.kuken.feature.instance.LogEntry
import gg.kuken.feature.instance.websocket.InstanceLogsConsoleSession
import gg.kuken.feature.instance.websocket.InstanceLogsConsoleSessionAttributeKey
import gg.kuken.websocket.WebSocketClientMessageContext
import gg.kuken.websocket.WebSocketClientMessageHandler
import gg.kuken.websocket.WebSocketOpCodes
import gg.kuken.websocket.long
import gg.kuken.websocket.respond
import gg.kuken.websocket.respondAsync
import gg.kuken.websocket.uuid
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.devnatan.dockerkt.DockerClient
import me.devnatan.dockerkt.models.Stream
import me.devnatan.dockerkt.models.container.ContainerLogsOptions
import me.devnatan.dockerkt.models.container.ContainerLogsResult
import me.devnatan.dockerkt.resource.container.ContainerNotFoundException
import kotlin.uuid.Uuid

class InstanceLogsRequestWSHandler(
    val instanceService: InstanceService,
    val dockerClient: DockerClient,
    val activityLogStore: ActivityLogStore,
) : WebSocketClientMessageHandler() {
    override suspend fun WebSocketClientMessageContext.handle() {
        val instanceId = packet.uuid("iid")
        val since = packet.long("since")

        val containerId =
            try {
                instanceService.getInstanceContainerId(instanceId)
            } catch (_: InstanceNotFoundException) {
                return respond(WebSocketOpCodes.InstanceUnavailable)
            }

        val consoleSession =
            session.connection.call.attributes.computeIfAbsent(InstanceLogsConsoleSessionAttributeKey) {
                InstanceLogsConsoleSession(instanceId)
            }

        try {
            captureLogs(instanceId, containerId, since) { frame ->
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
        instanceId: Uuid,
        containerId: String,
        since: Long,
        onFrame: (LogEntry) -> Unit,
    ) {
        coroutineScope {
            launch {
                fetchRuntimeLogs(containerId, since, onFrame)
            }

            launch {
                activityLogStore
                    .query(
                        resource = ResourceId(instanceId),
                    ).forEach(onFrame)
            }
        }
    }

    private suspend fun fetchRuntimeLogs(
        containerId: String,
        since: Long,
        onFrame: (LogEntry) -> Unit,
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
                .map { frame -> LogEntry.Console.fromText(frame.value.removeSuffix("\n"), frame.stream) }
                .let { flow ->
                    if (since > 0) {
                        flow.filter { frame -> frame.ts >= since }
                    } else {
                        flow
                    }
                }.collect { frame -> onFrame(frame) }
        } catch (_: CancellationException) {
            // Session closed while fetching logs
        }
    }
}
