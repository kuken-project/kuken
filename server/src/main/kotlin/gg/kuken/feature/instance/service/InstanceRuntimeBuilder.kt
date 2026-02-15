package gg.kuken.feature.instance.service

import gg.kuken.feature.instance.model.InstanceRuntime
import gg.kuken.feature.instance.model.InstanceRuntimeMount
import gg.kuken.feature.instance.model.InstanceRuntimeNetwork
import gg.kuken.feature.instance.model.InstanceRuntimeSingleNetwork
import me.devnatan.dockerkt.DockerClient
import me.devnatan.dockerkt.resource.container.ContainerNotFoundException

/**
 * Builds InstanceRuntime models from Docker container inspection data.
 *
 * Responsible for mapping Docker container state, network settings, and mount information
 * into the application's InstanceRuntime domain model.
 */
class InstanceRuntimeBuilder(
    private val dockerClient: DockerClient,
) {
    /**
     * Builds a runtime model for a container.
     *
     * @param containerId The Docker container ID
     * @return The instance runtime model
     * @throws ContainerNotFoundException if the container does not exist
     */
    suspend fun buildRuntime(containerId: String): InstanceRuntime {
        val inspection = dockerClient.containers.inspect(containerId)
        val networkSettings = inspection.networkSettings
        val state = inspection.state

        return InstanceRuntime(
            id = inspection.id,
            network =
                InstanceRuntimeNetwork(
                    ipV4Address =
                        networkSettings.ports.entries
                            .firstOrNull()
                            ?.value
                            ?.firstOrNull()
                            ?.ip
                            .orEmpty(),
                    hostname = inspection.config.hostname,
                    networks =
                        networkSettings.networks.map { (name, settings) ->
                            InstanceRuntimeSingleNetwork(
                                id = settings.networkID ?: "",
                                name = name,
                                ipv4Address = settings.ipamConfig?.ipv4Address?.ifBlank { null },
                                ipv6Address = settings.ipamConfig?.ipv6Address?.ifBlank { null },
                            )
                        },
                ),
            platform = inspection.platform.ifBlank { null },
            exitCode = state.exitCode ?: 0,
            pid = state.pid ?: 0,
            startedAt = state.startedAt,
            finishedAt = state.finishedAt,
            error = state.error?.ifBlank { null },
            status = state.status.value,
            fsPath = null, // TODO missing property
            outOfMemory = state.oomKilled,
            mounts =
                inspection.mounts.map { mount ->
                    InstanceRuntimeMount(
                        type = mount.type.name,
                        source = mount.source!!,
                        destination = mount.target,
                        readonly = mount.readonly,
                    )
                },
        )
    }

    /**
     * Attempts to build a runtime model for a container, returning null if the container is not found.
     *
     * @param containerId The Docker container ID
     * @return The instance runtime model or null if the container does not exist
     */
    suspend fun tryBuildRuntime(containerId: String): InstanceRuntime? =
        try {
            buildRuntime(containerId)
        } catch (_: ContainerNotFoundException) {
            null
        }
}
