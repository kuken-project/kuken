package gg.kuken.core.io.util

import gg.kuken.core.io.FileEntry
import gg.kuken.core.io.FileType
import kotlin.time.Instant

private const val FIELD_SEPARATOR = '|'

class StatFileEntryParser {
    fun parse(output: String): List<FileEntry> =
        output
            .lines()
            .filter { it.isNotBlank() }
            .map { line -> line.removePrefix("\'").removeSuffix("\'") }
            .mapNotNull { line -> parseLine(line) }

    fun parseLine(line: String): FileEntry? {
        val fields = line.split(FIELD_SEPARATOR)
        if (fields.size < 7) return null

        val name = fields[0]
        val typeStr = fields[1]
        val size = fields[2].toLongOrNull() ?: 0L
        val permissions = fields[3]
        val birthTimestamp = fields[4].toLongOrNull()
        val accessTimestamp = fields[5].toLongOrNull() ?: 0L
        val modifyTimestamp = fields[6].toLongOrNull() ?: 0L

        val type = parseFileType(typeStr)
        val perms = parsePermissions(permissions)

        return FileEntry(
            relativePath = name,
            name = name.substringAfterLast("/"),
            size = size,
            type = type,
            createdAt =
                birthTimestamp?.let { time ->
                    if (time > 0) timestampToDateTime(time) else null
                },
            accessedAt = timestampToDateTime(accessTimestamp),
            modifiedAt = timestampToDateTime(modifyTimestamp),
            permissions = permissions,
            isExecutable = perms.executable,
            isReadable = perms.readable,
            isWritable = perms.writable,
        )
    }

    private fun timestampToDateTime(timestamp: Long): Instant = Instant.fromEpochSeconds(timestamp)

    private fun parseFileType(typeStr: String): FileType =
        when {
            typeStr.contains("directory", ignoreCase = true) -> FileType.DIRECTORY
            typeStr.contains("symbolic link", ignoreCase = true) -> FileType.SYMLINK
            typeStr.contains("regular", ignoreCase = true) -> FileType.FILE
            else -> FileType.OTHER
        }

    private fun parsePermissions(octal: String): PermissionFlags {
        val ownerPerm = octal.firstOrNull()?.digitToIntOrNull() ?: 0

        return PermissionFlags(
            readable = (ownerPerm and 4) != 0, // 4 = 100 (read)
            writable = (ownerPerm and 2) != 0, // 2 = 010 (write)
            executable = (ownerPerm and 1) != 0, // 1 = 001 (execute)
        )
    }

    private data class PermissionFlags(
        val readable: Boolean,
        val writable: Boolean,
        val executable: Boolean,
    )
}
