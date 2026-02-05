package gg.kuken.feature.instance

import gg.kuken.KukenConfig
import gg.kuken.core.io.FileEntry
import gg.kuken.core.io.FileSystem
import gg.kuken.core.io.safePath
import me.devnatan.dockerkt.DockerClient
import java.nio.file.Path
import kotlin.uuid.Uuid

class InstanceFileService(
    val instanceService: InstanceService,
    val dockerClient: DockerClient,
    val config: KukenConfig,
) {
    suspend fun fileSystemOf(instanceId: Uuid): FileSystem {
        val containerId = instanceService.getInstanceContainerId(instanceId)

        // FIXME val isRunning = dockerClient.containers.inspect(containerId).state.isRunning
        // FIXME isRunning
        return if (false) {
            InProcessDockerContainerFileSystem(containerId, dockerClient)
        } else {
            HostDockerContainerFileSystem(instanceId, dockerClient, config)
        }
    }

    suspend fun resolve(
        instanceId: Uuid,
        path: String,
    ): Path = with(fileSystemOf(instanceId)) { safePath(path) }

    suspend fun getFile(
        instanceId: Uuid,
        filePath: String,
    ) = fileSystemOf(instanceId).getFile(filePath)

    suspend fun listFiles(
        instanceId: Uuid,
        filePath: String,
    ): List<FileEntry> = fileSystemOf(instanceId).listDirectory(filePath)

    suspend fun readFile(
        instanceId: Uuid,
        filePath: String,
    ): String = fileSystemOf(instanceId).readFileContents(filePath)

    suspend fun writeFile(
        instanceId: Uuid,
        filePath: String,
        contents: String,
    ) {
        fileSystemOf(instanceId).writeFileContents(filePath, contents)
    }

    suspend fun deleteFile(
        instanceId: Uuid,
        filePath: String,
    ) = fileSystemOf(instanceId).deleteFile(filePath)

    suspend fun renameFile(
        instanceId: Uuid,
        filePath: String,
        newName: String,
    ) = fileSystemOf(instanceId).renameFile(filePath, newName)

    suspend fun touchFile(
        instanceId: Uuid,
        filePath: String,
    ): String = fileSystemOf(instanceId).touchFile(filePath)
}
