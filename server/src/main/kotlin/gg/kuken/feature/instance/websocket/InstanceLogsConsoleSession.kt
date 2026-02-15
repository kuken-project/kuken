package gg.kuken.feature.instance.websocket

import gg.kuken.feature.instance.LogEntry
import io.ktor.util.AttributeKey
import kotlinx.atomicfu.atomic
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.uuid.Uuid

private const val DEFAULT_MAX_BUFFER_SIZE = 50_000
internal val InstanceLogsConsoleSessionAttributeKey =
    AttributeKey<InstanceLogsConsoleSession>("instance-console-session")

class InstanceLogsConsoleSession(
    val instanceId: Uuid,
    val maxBufferSize: Int = DEFAULT_MAX_BUFFER_SIZE,
) {
    private val seqCounter = atomic(0L)
    private val frameBuffer = ConcurrentLinkedDeque<LogEntry>()

    fun addLog(frame: LogEntry): LogEntry {
        val seqId = seqCounter.incrementAndGet()
        val frame =
            when (frame) {
                is LogEntry.Activity -> frame.copy(seqId = seqId)
                is LogEntry.Console -> frame.copy(seqId = seqId)
            }

        frameBuffer.addLast(frame)

        while (frameBuffer.size > maxBufferSize) {
            frameBuffer.pollFirst()
        }

        return frame
    }

    fun getFramesBefore(
        beforeSeqId: Long,
        limit: Int,
    ): Pair<List<LogEntry>, Boolean> {
        val frames =
            frameBuffer
                .filter { frame -> frame.seqId < beforeSeqId }
                .sortedByDescending { frame -> frame.seqId }
                .take(limit)
                .reversed()

        val oldest = frameBuffer.peekFirst()?.seqId ?: 0
        val hasMore = frames.isNotEmpty() && frames.first().seqId > oldest

        return frames to hasMore
    }

    fun getFramesAfter(
        afterSeqId: Long,
        limit: Int,
    ): Pair<List<LogEntry>, Boolean> {
        val frames =
            frameBuffer
                .filter { frame -> frame.seqId > afterSeqId }
                .sortedBy { frame -> frame.seqId }
                .take(limit)

        val newsest = frameBuffer.peekLast()?.seqId ?: 0
        val hasMore = frames.isNotEmpty() && frames.last().seqId < newsest

        return frames to hasMore
    }

    fun getFramesAround(
        timestamp: Long,
        limit: Int,
    ): List<LogEntry> {
        val halfLimit = limit / 2
        val allFrames = frameBuffer.toList()
        val pivot =
            allFrames
                .indexOfFirst { frame -> frame.ts >= timestamp }
                .takeIf { idx -> idx >= 0 } ?: allFrames.lastIndex

        val startIdx = (pivot - halfLimit).coerceAtLeast(0)
        val endIdx = (pivot + halfLimit).coerceAtMost(allFrames.lastIndex)

        return allFrames.subList(startIdx, endIdx + 1).toList()
    }

    fun getRecentFrames(limit: Int): List<LogEntry> = frameBuffer.toList().takeLast(limit)
}
