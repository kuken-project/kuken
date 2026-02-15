package gg.kuken.feature.instance.util

object FramePersistentIdGenerator {
    fun generate(
        timestamp: Long,
        content: String,
        streamCode: Int,
    ): String {
        val input = "$timestamp:$streamCode:$content"
        return "$timestamp-${input.hashCode()}"
    }

    fun generate(
        timestamp: Long,
        content: String,
    ): String = generate(timestamp, content, 0)
}
