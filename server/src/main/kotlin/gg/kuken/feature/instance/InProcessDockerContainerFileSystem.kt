package gg.kuken.feature.instance

import gg.kuken.KukenConfig
import gg.kuken.core.io.FileEntry
import gg.kuken.core.io.FileSystem
import gg.kuken.core.io.safePath
import gg.kuken.core.io.util.StatFileEntryParser
import me.devnatan.dockerkt.DockerClient
import me.devnatan.dockerkt.models.exec.ExecStartOptions
import me.devnatan.dockerkt.models.exec.ExecStartResult
import me.devnatan.dockerkt.resource.exec.create
import me.devnatan.dockerkt.resource.exec.start
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.deleteRecursively
import kotlin.io.path.pathString

private const val FILE_SEPARATOR = "/"

class InProcessDockerContainerFileSystem(
    val containerId: String,
    val dockerClient: DockerClient,
) : FileSystem {
    override val root: Path get() = error("Not supported")
    val statFileEntryParser = StatFileEntryParser()

    override suspend fun getFile(path: String): FileEntry = throw UnsupportedOperationException("Not supported")

    override suspend fun listDirectory(path: String): List<FileEntry> {
        val execId =
            dockerClient.exec.create(containerId) {
                command = listOf("sh", "-c", "stat --printf '%n|%F|%s|%a|%W|%X|%Y\\n' $path")
                attachStdout = true
            }

        val result = dockerClient.exec.start(execId, ExecStartOptions())
        require(result is ExecStartResult.Complete)

        return statFileEntryParser.parse(result.output)
    }

    override suspend fun readFileContents(path: String): String {
        val execId =
            dockerClient.exec.create(containerId) {
                command = listOf("cat", path)
                attachStdout = true
            }

        val result = dockerClient.exec.start(execId, ExecStartOptions())
        require(result is ExecStartResult.Complete)

        return result.output
    }

    override suspend fun writeFileContents(
        path: String,
        contents: String,
    ) {
        val fileDirectory = path.substringBeforeLast(FILE_SEPARATOR)
        val tempDir = KukenConfig.tempDirRecursively(fileDirectory)

        val workingDir =
            requireNotNull(
                dockerClient.containers
                    .inspect(containerId)
                    .config.workingDir,
            ) {
                "Docker container working directory is not set"
            }

        // TODO Preserve original file permissions and line breaks
        try {
            val fileName = path.substringAfterLast(FILE_SEPARATOR)
            val tempFile = tempDir.resolve(fileName).toFile()
            tempFile.writeText(contents)

            val destPath =
                if (path.contains(FILE_SEPARATOR)) {
                    workingDir + FILE_SEPARATOR + fileDirectory
                } else {
                    workingDir
                }
            dockerClient.containers.copyFileTo(
                container = containerId,
                sourcePath = tempFile.absolutePath,
                destinationPath = destPath,
            )
        } finally {
            @OptIn(ExperimentalPathApi::class)
            tempDir.deleteRecursively()
        }
    }

    override suspend fun deleteFile(path: String) {
        val execId =
            dockerClient.exec.create(containerId) {
                command = listOf("rm", "-f", path)
            }

        dockerClient.exec.start(execId)
    }

    override suspend fun renameFile(
        path: String,
        newName: String,
    ) {
        val parent = Path(path).parent
        val expectedNewPath = parent.resolve(newName)
        val fixedNewPath = safePath(expectedNewPath.pathString)

        val execId =
            dockerClient.exec.create(containerId) {
                command = listOf("cp", path, fixedNewPath.pathString)
            }

        dockerClient.exec.start(execId)
    }

    override suspend fun touchFile(path: String): String {
        val execId =
            dockerClient.exec.create(containerId) {
                command = listOf("touch", path)
            }

        dockerClient.exec.start(execId)
        return path
    }
}
