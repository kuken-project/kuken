package gg.kuken.core.io

import kotlinx.io.files.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.pathString

interface FileSystem {
    val root: Path

    suspend fun getFile(path: String): FileEntry

    suspend fun listDirectory(path: String): List<FileEntry>

    suspend fun readFileContents(path: String): String

    suspend fun writeFileContents(
        path: String,
        contents: String,
    )

    suspend fun deleteFile(path: String)

    suspend fun renameFile(
        path: String,
        newName: String,
    )

    suspend fun touchFile(path: String): String
}

context(_: FileSystem)
fun requireExists(path: Path): Path {
    if (!path.exists()) {
        throw FileNotFoundException("$path not found")
    }

    return path
}

context(fs: FileSystem)
fun safePath(relativePath: String): Path {
    val normalized = relativePath.replace("\\", "/").removePrefix("/")
    val resolved = fs.root.resolve(normalized).normalize()

    require(resolved.startsWith(fs.root)) {
        "Relative path access not allowed"
    }

    return resolved
}

context(_: FileSystem)
fun requireSafePath(path: String): Path = requireExists(safePath(path))
