package gg.kuken.core.io

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class FileEntry(
    val relativePath: String,
    val name: String,
    val size: Long,
    val type: FileType,
    val createdAt: Instant?,
    val accessedAt: Instant,
    val modifiedAt: Instant,
    val permissions: String,
    val isExecutable: Boolean,
    val isReadable: Boolean,
    val isWritable: Boolean,
) {
    val isDirectory: Boolean
        get() = type == FileType.DIRECTORY

    val isFile: Boolean
        get() = type == FileType.FILE

    val isSymlink: Boolean
        get() = type == FileType.SYMLINK
}
