package gg.kuken.core.docker

import gg.kuken.feature.instance.model.DockerImagePullStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import me.devnatan.dockerkt.DockerClient
import me.devnatan.dockerkt.resource.image.ImageNotFoundException
import org.apache.logging.log4j.LogManager

/**
 * Implementation of DockerImageService using DockerClient.
 */
class DockerImageServiceImpl(
    private val dockerClient: DockerClient,
) : DockerImageService {
    private val logger = LogManager.getLogger(DockerImageServiceImpl::class.java)

    override fun pullImage(image: String): Flow<DockerImagePullStatus> =
        flow {
            dockerClient.images
                .pull(image)
                .onStart {
                    logger.debug("Pulling image $image")
                    emit(DockerImagePullStatus.Started)
                }.catch { exception ->
                    logger.error("Failed to pull image: {}", image, exception)
                    if (exception is ImageNotFoundException) {
                        emit(DockerImagePullStatus.NotFound)
                        currentCoroutineContext().cancel()
                    } else {
                        emit(DockerImagePullStatus.Failed)
                    }
                }.onCompletion {
                    emit(DockerImagePullStatus.Completed)
                    logger.debug("Image {} pull completed.", image)
                }.flowOn(Dispatchers.IO)
                .collect { pull ->
                    logger.debug(
                        "{} {}: {}/{} ({})",
                        pull.statusText,
                        image,
                        pull.progressDetail?.current ?: "???",
                        pull.progressDetail?.total ?: "???",
                        pull.id,
                    )
                    emit(DockerImagePullStatus.Progress(pull))
                }
        }
}
