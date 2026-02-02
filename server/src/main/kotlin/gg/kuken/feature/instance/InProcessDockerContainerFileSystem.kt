package gg.kuken.feature.instance

import gg.kuken.KukenConfig
import gg.kuken.core.io.FileEntry
import gg.kuken.core.io.FileSystem
import gg.kuken.core.io.util.StatFileEntryParser
import me.devnatan.dockerkt.DockerClient
import me.devnatan.dockerkt.models.exec.ExecStartOptions
import me.devnatan.dockerkt.models.exec.ExecStartResult
import me.devnatan.dockerkt.resource.container.copyDirectoryTo
import me.devnatan.dockerkt.resource.container.copyFileTo
import me.devnatan.dockerkt.resource.container.copyTo
import me.devnatan.dockerkt.resource.exec.create
import me.devnatan.dockerkt.resource.exec.start
import java.io.File
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.pathString
import kotlin.io.path.writeText

private const val FILE_SEPARATOR = "/"

class InProcessDockerContainerFileSystem(
    val containerId: String,
    val dockerClient: DockerClient,
) : FileSystem {
    val statFileEntryParser = StatFileEntryParser()

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
        val tempDir = KukenConfig.tempDir(Paths.get(fileDirectory))

        val workingDir =
            dockerClient.containers
                .inspect(containerId)
                .config.workingDir

        try {
            val fileName = path.substringAfterLast(FILE_SEPARATOR)
            val tempFile = tempDir.resolve(fileName).toFile()
            tempFile.writeText(contents)

            dockerClient.containers.copyDirectoryTo(
                container = containerId,
                sourcePath = tempDir.pathString,
                destinationPath = workingDir + FILE_SEPARATOR + fileDirectory,
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
}
