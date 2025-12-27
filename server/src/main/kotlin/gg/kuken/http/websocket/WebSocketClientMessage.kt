package gg.kuken.http.websocket

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.serializer
import kotlin.uuid.Uuid

@Serializable
data class WebSocketClientMessage(
    @SerialName("o") @Required val op: Int,
    @SerialName("d") val data: JsonObject? = null,
)

data class WebSocketClientMessageContext(
    val packet: WebSocketClientMessage,
    val session: WebSocketSession,
)

suspend fun WebSocketClientMessageContext.respond(op: Int) {
    respond<Unit>(op = op, data = null)
}

suspend inline fun <reified T> WebSocketClientMessageContext.respond(
    op: Int = packet.op,
    data: T? = null,
) {
    session.send(
        serializer = WebSocketServerMessageSerializer(serializer<T>()),
        message = WebSocketServerMessage(op = op, data = data),
    )
}

context(_: WebSocketClientMessageContext)
fun WebSocketClientMessage.string(key: String): String {
    val text = data?.get(key) as? JsonPrimitive
    return text?.contentOrNull ?: error("Required key $key not found in packet data")
}

context(_: WebSocketClientMessageContext)
fun WebSocketClientMessage.uuid(key: String): Uuid {
    val text = string(key)
    return Uuid.parse(text)
}
