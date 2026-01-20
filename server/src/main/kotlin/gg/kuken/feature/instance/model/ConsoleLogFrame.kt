package gg.kuken.feature.instance.model

import gg.kuken.feature.instance.util.FramePersistentIdGenerator
import kotlinx.serialization.Serializable
import me.devnatan.dockerkt.models.Stream
import java.time.Instant

@Serializable
data class ConsoleLogFrame(
    val seqId: Long,
    val persistentId: String,
    val value: String,
    val stream: Stream,
    val timestamp: Long,
) {
    companion object {
        fun fromText(
            content: String,
            stream: Stream,
        ): ConsoleLogFrame {
            val timestamp = Instant.parse(content.substringBefore(" ")).toEpochMilli()
            return ConsoleLogFrame(
                seqId = -1,
                persistentId =
                    FramePersistentIdGenerator.generate(
                        timestamp = timestamp,
                        content = content,
                        streamCode =
                            when (stream.name) {
                                Stream.StdIn.name -> 0
                                Stream.StdOut.name -> 1
                                Stream.StdErr.name -> 2
                                else -> 1 // stdout
                            },
                    ),
                value = content.substringAfter(" "),
                stream = stream,
                timestamp = timestamp,
            )
        }
    }
}
