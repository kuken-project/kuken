package gg.kuken.http.websocket

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import java.nio.channels.ClosedChannelException
import java.util.Collections
import kotlin.reflect.jvm.jvmName

class WebSocketManager(
    val json: Json,
) : CoroutineScope by CoroutineScope(
        SupervisorJob() + CoroutineName(WebSocketManager::class.jvmName),
    ) {
    private val logger = LogManager.getLogger(WebSocketManager::class.java)
    private val sessions = Collections.synchronizedSet<WebSocketSession>(linkedSetOf())
    private val nextSessionId = atomic(0)
    private val handlers = mutableMapOf<WebSocketOp, MutableList<WebSocketClientMessageHandler>>()

    suspend fun connect(connection: DefaultWebSocketServerSession) {
        val session = WebSocketSession(nextSessionId.getAndIncrement(), connection, json)
        sessions.add(session)
        logger.debug(
            "WebSocket session {} connected @ {}",
            session.id,
            session.connection.call.request.local.remoteHost,
        )

        try {
            for (frame in connection.incoming) {
                if (frame is Frame.Text) {
                    val packet = decodePacket(frame) ?: continue
                    packetReceived(packet, session)
                }
            }
        } catch (_: ClosedReceiveChannelException) {
            val closeReason = session.connection.closeReason.await()
            logger.debug("WebSocket session receive channel closed: {}", closeReason)
        } catch (e: Throwable) {
            val closeReason = session.connection.closeReason.await()
            logger.error("WebSocket session handling uncaught error: $closeReason", e)
        } finally {
            logger.debug("WebSocket session ${session.id} closed")
            detach(session)
        }
    }

    private fun decodePacket(frame: Frame.Text): WebSocketClientMessage? =
        try {
            json.decodeFromString<WebSocketClientMessage>(frame.readText())
        } catch (e: SerializationException) {
            logger.error("Failed to deserialize WebSocket packet text", e)
            null
        }

    private suspend fun packetReceived(
        packet: WebSocketClientMessage,
        session: WebSocketSession,
    ) {
        val handlerList = handlers[packet.op]
        if (handlerList.isNullOrEmpty()) {
            logger.warn("No WebSocket client message handler registered for op {}", packet.op)
            return
        }

        val context = WebSocketClientMessageContext(packet, session)
        handlerList.forEach { handler ->
            try {
                with(handler) { context.handle() }
            } catch (e: Throwable) {
                logger.error(
                    "Uncaught exception in WebSocket client message handler in {} (op {})",
                    handler::class.qualifiedName,
                    packet.op,
                    e,
                )
            }
        }
    }

    private suspend fun detach(session: WebSocketSession) {
        try {
            session.connection.close()
        } catch (_: ClosedChannelException) {
            // Channel already closed
        } finally {
            sessions.remove(session)
        }
    }

    fun register(
        op: WebSocketOp,
        handler: WebSocketClientMessageHandler,
    ) {
        handler.coroutineContext = Job() + CoroutineName("$op-${handler::class.simpleName ?: "unknown"}")
        handlers.computeIfAbsent(op) { mutableListOf() }.add(handler)
    }
}
