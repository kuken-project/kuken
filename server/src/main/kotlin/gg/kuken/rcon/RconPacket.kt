package gg.kuken.rcon

import java.nio.ByteBuffer
import java.nio.ByteOrder

const val RCON_PACKET_HEADER_SIZE = 10 // 4 (id) + 4 (type) + 2 (null terminators)
const val RCON_PACKET_MAX_BODY_SIZE = 4096

// Packet types (see https://developer.valvesoftware.com/wiki/Source_RCON_Protocol)
const val RCON_SERVERDATA_AUTH = 3
const val RCON_SERVERDATA_AUTH_RESPONSE = 2
const val RCON_SERVERDATA_EXECCOMMAND = 2
const val RCON_SERVERDATA_RESPONSE_VALUE = 0

data class RconPacket(
    val id: Int,
    val type: Int,
    val body: String,
) {
    object Codec {
        fun encode(packet: RconPacket): ByteArray {
            val bodyBytes = packet.body.toByteArray(Charsets.UTF_8)
            val packetSize = 4 + 4 + bodyBytes.size + 2 // id + type + body + 2 null terminators

            val buffer = ByteBuffer.allocate(4 + packetSize)
            buffer.order(ByteOrder.LITTLE_ENDIAN)

            buffer.putInt(packetSize) // Size
            buffer.putInt(packet.id) // Request ID
            buffer.putInt(packet.type) // Type
            buffer.put(bodyBytes) // Body
            buffer.put(0) // Null terminator
            buffer.put(0) // Empty string terminator

            return buffer.array()
        }

        fun decode(data: ByteArray): RconPacket? {
            if (data.size < RCON_PACKET_HEADER_SIZE + 4) {
                return null
            }

            val buffer = ByteBuffer.wrap(data)
            buffer.order(ByteOrder.LITTLE_ENDIAN)

            val size = buffer.getInt()

            if (data.size < size + 4) {
                return null
            }

            val id = buffer.getInt()
            val type = buffer.getInt()

            val bodyLength = size - 4 - 4 - 2
            val bodyBytes = ByteArray(bodyLength.coerceAtLeast(0))
            buffer.get(bodyBytes)

            val body = String(bodyBytes, Charsets.UTF_8)

            return RconPacket(id, type, body)
        }

        fun decodeMultiple(data: ByteArray): List<RconPacket> {
            val packets = mutableListOf<RconPacket>()
            var offset = 0

            while (offset < data.size - 4) {
                val buffer = ByteBuffer.wrap(data, offset, 4)
                buffer.order(ByteOrder.LITTLE_ENDIAN)
                val size = buffer.getInt()

                val packetEnd = offset + 4 + size
                if (packetEnd > data.size) break

                val packetData = data.copyOfRange(offset, packetEnd)
                decode(packetData)?.let { packets.add(it) }

                offset = packetEnd
            }

            return packets
        }

        fun getExpectedSize(header: ByteArray): Int {
            if (header.size < 4) return -1

            val buffer = ByteBuffer.wrap(header, 0, 4)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            return buffer.getInt() + 4 // size field + packet data
        }
    }
}
