package gg.kuken.core.docker

import gg.kuken.feature.instance.model.DockerImagePullStatus
import kotlinx.coroutines.flow.Flow

/**
 * Service for managing Docker image operations.
 *
 * Handles pulling Docker images and tracking pull progress.
 */
interface DockerImageService {
    /**
     * Pulls a Docker image and emits status updates.
     *
     * @param image The Docker image name to pull
     * @return A flow of ImagePullStatus updates
     */
    fun pullImage(image: String): Flow<DockerImagePullStatus>
}
