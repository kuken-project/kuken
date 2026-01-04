package gg.kuken.feature.instance

import gg.kuken.core.io.FileEntry
import gg.kuken.core.io.FileSystem
import me.devnatan.dockerkt.DockerClient
import org.koin.core.component.KoinComponent
import kotlin.uuid.Uuid

class InstanceFileService(
    val instanceService: InstanceService,
    val dockerClient: DockerClient,
) : KoinComponent {
    suspend fun fileSystemOf(instanceId: Uuid): FileSystem {
        val containerId = instanceService.getInstanceContainerId(instanceId)
        val fileSystem = InProcessDockerContainerFileSystem(containerId, dockerClient)

        return fileSystem
    }

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
}
