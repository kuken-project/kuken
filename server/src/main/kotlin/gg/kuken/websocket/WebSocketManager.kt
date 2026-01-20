package gg.kuken.websocket

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
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
                    val message = decodeMessage(frame) ?: continue
                    messageReceived(message, session)
                }
            }
        } catch (_: ClosedReceiveChannelException) {
            val closeReason = session.connection.closeReason.await()
            logger.debug("WebSocket session receive channel closed: {}", closeReason)
        } catch (e: Throwable) {
            val closeReason = session.connection.closeReason.await()
            logger.error("WebSocket session handling uncaught error: $closeReason", e)
        } finally {
            detach(session)
        }
    }

    private fun decodeMessage(frame: Frame.Text): WebSocketClientMessage? =
        try {
            json.decodeFromString<WebSocketClientMessage>(frame.readText())
        } catch (e: SerializationException) {
            logger.error("Failed to deserialize WebSocket message", e)
            null
        }

    private fun messageReceived(
        message: WebSocketClientMessage,
        session: WebSocketSession,
    ) {
        val handlerList = handlers[message.op]
        if (handlerList.isNullOrEmpty()) {
            logger.warn("No WebSocket client message handler registered for op {}", message.op)
            return
        }

        val context = WebSocketClientMessageContext(message, session)
        handlerList.forEach { handler ->
            with(handler) { handleMessage(session, context, message) }
        }
    }

    private fun WebSocketClientMessageHandler.handleMessage(
        session: WebSocketSession,
        context: WebSocketClientMessageContext,
        message: WebSocketClientMessage,
    ) {
        // Use connection as coroutine context so handlers get cancelled when
        // session underlyning connection gets closed in #detach()
        @Suppress("CoroutineContextWithJob")
        launch(session.connection.coroutineContext) {
            try {
                context.handle()
            } catch (e: Throwable) {
                // Catch any exceptions so parent coroutine don't get cancelled
                logger.error(
                    "Uncaught exception in WebSocket session message handler. Session: {}. Op: {}. Handler: {}",
                    session.id,
                    message.op,
                    this@handleMessage::class.qualifiedName,
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

        logger.debug("WebSocket session ${session.id} closed")
    }

    fun register(
        op: WebSocketOp,
        handler: WebSocketClientMessageHandler,
    ) {
        handlers.computeIfAbsent(op) { mutableListOf() }.add(handler)
    }

    internal suspend inline fun broadcasting(crossinline block: suspend (WebSocketSession) -> Unit) =
        supervisorScope {
            sessions.forEach { session -> block(session) }
        }

    fun isReceivingEvents(): Boolean = sessions.isNotEmpty()
}
