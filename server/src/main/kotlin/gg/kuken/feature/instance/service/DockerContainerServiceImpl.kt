package gg.kuken.feature.instance.service

import gg.kuken.core.docker.DockerContainerService
import gg.kuken.feature.blueprint.processor.AppResource
import gg.kuken.feature.instance.model.CreateInstanceOptions
import me.devnatan.dockerkt.DockerClient
import me.devnatan.dockerkt.models.PortBinding
import me.devnatan.dockerkt.models.container.hostConfig
import me.devnatan.dockerkt.models.portBindings
import me.devnatan.dockerkt.resource.container.create
import me.devnatan.dockerkt.resource.container.remove
import me.devnatan.dockerkt.resource.exec.create
import me.devnatan.dockerkt.resource.exec.start
import org.apache.logging.log4j.LogManager
import kotlin.uuid.Uuid

/**
 * Implementation of DockerContainerService using DockerClient.
 */
class DockerContainerServiceImpl(
    private val dockerClient: DockerClient,
    private val instanceInstaller: InstanceInstaller,
) : DockerContainerService {
    private val logger = LogManager.getLogger(DockerContainerServiceImpl::class.java)

    override suspend fun createContainer(
        instanceId: Uuid,
        name: String,
        image: String,
        options: CreateInstanceOptions,
        onInstall: AppResource?,
    ): String {
        logger.debug("Creating runtime {} (image: {})...", instanceId, image)
        requireNotNull(options.address.port) { "Port cannot be null" }

        val workingDir =
            dockerClient.images
                .inspect(image)
                .config.workingDir ?: error("Working directory not set")

        val installationDir = instanceInstaller.install(workingDir, instanceId, onInstall)

        val containerId =
            dockerClient.containers.create {
                this@create.name = name
                this@create.image = image

                labels = mapOf("gg.kuken.instance.id" to instanceId.toString())
                env = options.env.map { (key, value) -> "$key=$value" }

                // TODO Allow random port assignment
                hostConfig {
                    portBindings(options.address.port) {
                        add(PortBinding(options.address.host, options.address.port))
                    }

                    binds = listOf("${installationDir.replace("\\", "/")}:$workingDir")
                }
            }

        dockerClient.containers.start(containerId)

        val chmod =
            dockerClient.exec.create(containerId) {
                command = listOf("chmod", "-R", "777", workingDir)
                attachStdout = true
            }
        dockerClient.exec.start(chmod)

        return containerId
    }

    override suspend fun startContainer(containerId: String) {
        dockerClient.containers.start(containerId)
    }

    override suspend fun stopContainer(containerId: String) {
        dockerClient.containers.stop(containerId)
    }

    override suspend fun removeContainer(
        containerId: String,
        force: Boolean,
    ) {
        dockerClient.containers.remove(container = containerId) {
            this.force = force
            removeAnonymousVolumes = true
        }
    }
}
