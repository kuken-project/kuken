package gg.kuken.feature.instance

import gg.kuken.core.io.FileEntry
import gg.kuken.core.io.FileSystem
import gg.kuken.core.io.util.StatFileEntryParser
import me.devnatan.dockerkt.DockerClient
import me.devnatan.dockerkt.models.exec.ExecStartOptions
import me.devnatan.dockerkt.models.exec.ExecStartResult
import me.devnatan.dockerkt.resource.exec.create
import me.devnatan.dockerkt.resource.exec.start

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
        val execId =
            dockerClient.exec.create(containerId) {
                command = listOf("cat", path)
                attachStdout = true
                attachStdin = true
            }

        dockerClient.exec.start(execId, ExecStartOptions())
    }

    override suspend fun deleteFile(path: String) {
        val execId =
            dockerClient.exec.create(containerId) {
                command = listOf("rm", "-f", path)
            }

        dockerClient.exec.start(execId)
    }
}
