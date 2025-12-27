package gg.kuken.http.websocket

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlin.reflect.jvm.jvmName

@Serializable(with = WebSocketServerMessageSerializer::class)
data class WebSocketServerMessage<T>(
    @SerialName("o") val op: WebSocketOp,
    @SerialName("d") val data: T?,
)

class WebSocketServerMessageSerializer<T>(
    private val dataSerializer: KSerializer<T>,
) : KSerializer<WebSocketServerMessage<T>> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor(WebSocketServerMessage::class.jvmName) {
            element<Int>("o")
            element("d", dataSerializer.descriptor)
        }

    override fun serialize(
        encoder: Encoder,
        value: WebSocketServerMessage<T>,
    ) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor = descriptor, index = 0, value = value.op)

            @OptIn(ExperimentalSerializationApi::class)
            encodeNullableSerializableElement(
                descriptor = descriptor,
                index = 1,
                serializer = dataSerializer,
                value = value.data,
            )
        }
    }

    override fun deserialize(decoder: Decoder): WebSocketServerMessage<T> =
        decoder.decodeStructure(descriptor) {
            val data = decodeSerializableElement(descriptor = descriptor, index = 0, deserializer = dataSerializer)
            val op = decodeIntElement(descriptor = descriptor, index = 1)
            return@decodeStructure WebSocketServerMessage(op, data)
        }
}
