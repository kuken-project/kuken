package gg.kuken.feature.instance.model

import me.devnatan.dockerkt.models.image.ImagePull

sealed class DockerImagePullStatus {
    /**
     * Pull operation has started.
     */
    object Started : DockerImagePullStatus()

    /**
     * The requested image was not found in the registry.
     */
    object NotFound : DockerImagePullStatus()

    /**
     * Pull operation failed due to an error.
     */
    object Failed : DockerImagePullStatus()

    /**
     * Pull operation completed successfully.
     */
    object Completed : DockerImagePullStatus()

    /**
     * Pull operation is in progress.
     *
     * @param pull The Docker image pull progress information
     */
    data class Progress(
        val pull: ImagePull,
    ) : DockerImagePullStatus()
}
