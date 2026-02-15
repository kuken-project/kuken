package gg.kuken.feature.instance

import gg.kuken.core.ResourceId
import gg.kuken.feature.instance.util.FramePersistentIdGenerator
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import me.devnatan.dockerkt.models.Stream
import java.io.RandomAccessFile
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.appendText
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

class ActivityLogStore(
    private val logDir: Path,
) {
    private val json =
        Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

    private val locks = atomic(mapOf<ResourceId, Mutex>())

    init {
        logDir.createDirectories()
    }

    private fun logFile(resource: ResourceId): Path =
        logDir.resolve("$resource.jsonl").also {
            if (!it.exists()) {
                it.createFile()
            }
        }

    private fun mutexFor(resource: ResourceId): Mutex {
        locks.value[resource]?.let { return it }

        val mutex = Mutex()
        locks.update { current ->
            if (resource in current) {
                current
            } else {
                current + (resource to mutex)
            }
        }

        return locks.value.getValue(resource)
    }

    suspend fun append(
        resource: ResourceId,
        entry: LogEntry,
    ) {
        val line = json.encodeToString(entry) + "\n"
        mutexFor(resource).withLock {
            withContext(Dispatchers.IO) {
                logFile(resource).appendText(line)
            }
        }
    }

    suspend fun appendBatch(
        resource: ResourceId,
        entries: List<LogEntry>,
    ) {
        if (entries.isEmpty()) return
        val bulk =
            buildString {
                for (entry in entries) {
                    append(json.encodeToString(entry))
                    appendLine()
                }
            }

        mutexFor(resource).withLock {
            withContext(Dispatchers.IO) {
                logFile(resource).appendText(bulk)
            }
        }
    }

    suspend fun query(
        resource: ResourceId,
        sinceMs: Long = 0,
        untilMs: Long = Long.MAX_VALUE,
        limit: Int = 1000,
        typeFilter: String? = null,
    ): List<LogEntry> {
        val file = logFile(resource)
        if (!file.exists()) return emptyList()

        return mutexFor(resource).withLock {
            withContext(Dispatchers.IO) {
                RandomAccessFile(file.toFile(), "r").use { raf ->
                    val fileLen = raf.length()
                    if (fileLen == 0L) return@use emptyList()

                    val startOffset = if (sinceMs <= 0L) 0L else binarySearchOffset(raf, sinceMs)
                    raf.seek(startOffset)

                    buildList {
                        while (size < limit) {
                            val raw = raf.readLine() ?: break
                            val line = String(raw.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
                            if (line.isBlank()) continue

                            val entry =
                                try {
                                    json.decodeFromString(LogEntry.serializer(), line)
                                } catch (_: SerializationException) {
                                    continue // skip malformed
                                }

                            if (entry.ts > untilMs) break
                            if (entry.ts < sinceMs) continue

                            val matches =
                                when (typeFilter) {
                                    "activity" -> entry is LogEntry.Activity
                                    "console" -> entry is LogEntry.Console
                                    else -> true
                                }
                            if (matches) add(entry)
                        }
                    }
                }
            }
        }
    }

    private fun binarySearchOffset(
        raf: RandomAccessFile,
        targetMs: Long,
    ): Long {
        var low = 0L
        var high = raf.length()

        while (low < high) {
            val mid = low + (high - low) / 2

            raf.seek(mid)
            if (mid != 0L) raf.readLine() // skip partial line

            val line = raf.readLine()

            if (line == null) {
                high = mid
                continue
            }

            val ts = extractTimestamp(line)
            if (ts != null && ts < targetMs) {
                low = raf.filePointer
            } else {
                high = mid
            }
        }

        // Snap to line boundary
        raf.seek(low)
        if (low != 0L) raf.readLine()
        return raf.filePointer.coerceAtMost(raf.length())
    }

    private inline fun String.indexOfFirstFrom(
        startIndex: Int,
        predicate: (Char) -> Boolean,
    ): Int {
        for (i in startIndex until length) {
            if (predicate(this[i])) return i
        }
        return -1
    }

    private fun extractTimestamp(line: String): Long? {
        val idx = line.indexOf("\"ts\"")
        if (idx == -1) return null
        val colon = line.indexOf(':', idx + 4)
        if (colon == -1) return null

        val numStart = line.indexOfFirstFrom(colon + 1) { it.isDigit() || it == '-' }
        if (numStart == -1) return null
        val numEnd = line.indexOfFirstFrom(numStart + 1) { !it.isDigit() }

        val numStr = if (numEnd == -1) line.substring(numStart) else line.substring(numStart, numEnd)
        return numStr.toLongOrNull()
    }

    suspend fun delete(resource: ResourceId) {
        mutexFor(resource).withLock {
            withContext(Dispatchers.IO) {
                logFile(resource).deleteIfExists()
            }
        }

        locks.update { it - resource }
    }
}

@Serializable
sealed interface LogEntry {
    val ts: Long
    val msg: String
    val seqId: Long
    val persistentId: String

    @Serializable
    @SerialName("activity")
    data class Activity(
        override val ts: Long,
        override val persistentId: String,
        val activity: ActivityType,
        val step: String,
        val progress: Int,
        override val msg: String,
        override val seqId: Long,
    ) : LogEntry

    @Serializable
    @SerialName("console")
    data class Console(
        override val seqId: Long,
        override val persistentId: String,
        override val msg: String,
        val stream: Stream,
        override val ts: Long,
    ) : LogEntry {
        companion object {
            fun fromText(
                content: String,
                stream: Stream,
            ): Console {
                val timestamp = Instant.parse(content.substringBefore(" ")).toEpochMilli()
                return Console(
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
                    msg = content.substringAfter(" "),
                    stream = stream,
                    ts = timestamp,
                )
            }
        }
    }
}

@Serializable
enum class ActivityType {
    @SerialName("install")
    INSTALL,

    @SerialName("update")
    UPDATE,

    @SerialName("backup")
    BACKUP,

    @SerialName("restore")
    RESTORE,
}

@Serializable
enum class Stream {
    @SerialName("stdout")
    STDOUT,

    @SerialName("stderr")
    STDERR,
}
