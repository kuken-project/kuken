package gg.kuken.websocket

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager

class WebSocketSession(
    val id: Int,
    val connection: DefaultWebSocketServerSession,
    @Transient private val json: Json,
) {
    private val logger = LogManager.getLogger(WebSocketSession::class.java)

    suspend fun <T> send(
        serializer: KSerializer<WebSocketServerMessage<T>>,
        message: WebSocketServerMessage<T>,
    ) {
        connection.outgoing.send(Frame.Text(json.encodeToString(serializer, message)))
        logger.debug("Sent message to WebSocket session {}", id)
    }

    fun <T> fireAndForget(
        serializer: KSerializer<WebSocketServerMessage<T>>,
        message: WebSocketServerMessage<T>,
    ) {
        connection.outgoing.trySend(Frame.Text(json.encodeToString(serializer, message)))
        logger.debug("Sent message to WebSocket session {}", id)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WebSocketSession

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + connection.hashCode()
        return result
    }
}
