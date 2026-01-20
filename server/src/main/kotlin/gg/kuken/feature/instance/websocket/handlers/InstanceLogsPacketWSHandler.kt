package gg.kuken.feature.instance.websocket.handlers

import gg.kuken.feature.instance.InstanceService
import gg.kuken.feature.instance.http.dto.FetchLogsResponse
import gg.kuken.feature.instance.websocket.InstanceLogsConsoleSessionAttributeKey
import gg.kuken.websocket.WebSocketClientMessageContext
import gg.kuken.websocket.WebSocketClientMessageHandler
import gg.kuken.websocket.WebSocketOpCodes
import gg.kuken.websocket.long
import gg.kuken.websocket.longOrNull
import gg.kuken.websocket.respond
import gg.kuken.websocket.respondAsync
import me.devnatan.dockerkt.DockerClient

class InstanceLogsPacketWSHandler(
    val instanceService: InstanceService,
    val dockerClient: DockerClient,
) : WebSocketClientMessageHandler() {
    override suspend fun WebSocketClientMessageContext.handle() {
        val consoleSession =
            session.connection.call.attributes
                .getOrNull(InstanceLogsConsoleSessionAttributeKey)
        if (consoleSession == null) {
            respond(WebSocketOpCodes.IllegalState)
            return
        }

        val limit = packet.long("limit").toInt()
        val before = packet.longOrNull("before")
        val after = packet.longOrNull("after")
        val around = packet.longOrNull("around")

        val response =
            when {
                before != null -> {
                    val (frames, hasMore) = consoleSession.getFramesBefore(before, limit)
                    FetchLogsResponse(frames, hasMore)
                }

                after != null -> {
                    val (frames, hasMore) = consoleSession.getFramesAfter(after, limit)
                    FetchLogsResponse(
                        frames,
                        hasMore,
                    )
                }

                around != null -> {
                    val frames = consoleSession.getFramesAround(around, limit)
                    FetchLogsResponse(frames, hasMore = true)
                }

                else -> {
                    val frames = consoleSession.getRecentFrames(limit)
                    val hasMore =
                        frames.isNotEmpty() && frames.first().seqId >
                            let {
                                val framesBefore = consoleSession.getFramesBefore(frames.first().seqId, 1)
                                framesBefore.first.firstOrNull()?.seqId ?: 0
                            }
                    FetchLogsResponse(frames, hasMore)
                }
            }

        respondAsync(
            op = WebSocketOpCodes.InstanceLogsPacket,
            data = response,
        )
    }
}
