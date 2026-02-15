package gg.kuken.core.docker

import gg.kuken.feature.blueprint.processor.AppResource
import gg.kuken.feature.instance.model.CreateInstanceOptions
import kotlin.uuid.Uuid

/**
 * Service for managing Docker container lifecycle operations.
 *
 * Handles creation, starting, stopping, and removal of Docker containers
 * for instances, including installation hook execution during creation.
 */
interface DockerContainerService {
    /**
     * Creates a Docker container for an instance.
     *
     * @param instanceId The instance ID
     * @param name The container name
     * @param image The Docker image to use
     * @param options Instance creation options including environment and port bindings
     * @param onInstall Optional installation hook to run before container creation
     * @return The created container ID
     */
    suspend fun createContainer(
        instanceId: Uuid,
        name: String,
        image: String,
        options: CreateInstanceOptions,
        onInstall: AppResource?,
    ): String

    /**
     * Starts a Docker container.
     *
     * @param containerId The container ID to start
     */
    suspend fun startContainer(containerId: String)

    /**
     * Stops a Docker container.
     *
     * @param containerId The container ID to stop
     */
    suspend fun stopContainer(containerId: String)

    /**
     * Removes a Docker container.
     *
     * @param containerId The container ID to remove
     * @param force Whether to force removal
     */
    suspend fun removeContainer(
        containerId: String,
        force: Boolean = true,
    )
}
