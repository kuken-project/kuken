package gg.kuken.feature.instance.service

import gg.kuken.KukenConfig
import gg.kuken.feature.blueprint.processor.AppResource
import me.devnatan.dockerkt.DockerClient
import me.devnatan.dockerkt.models.container.hostConfig
import me.devnatan.dockerkt.models.exec.ExecStartOptions
import me.devnatan.dockerkt.models.exec.ExecStartResult
import me.devnatan.dockerkt.resource.container.create
import me.devnatan.dockerkt.resource.container.remove
import me.devnatan.dockerkt.resource.exec.create
import me.devnatan.dockerkt.resource.exec.start
import me.devnatan.dockerkt.resource.volume.create
import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.pathString
import kotlin.random.Random
import kotlin.random.nextUInt
import kotlin.uuid.Uuid

private const val LOCAL_RESOURCES_DIR = ".kuken/resources"
private const val DEFAULT_DOCKER_INSTALL_WORKING_DIR = "/mnt/server"
private const val DOCKER_INSTALL_IMAGE = "alpine:latest"
private const val DOCKER_INSTALL_CONTAINER_FORMAT = "kk-installer-%s"
private const val DOCKER_INSTALL_VOLUME_FORMAT = "kk-volume-%s"

/**
 * Handles instance installation by running installation hooks/scripts in temporary containers.
 *
 * Manages the creation of Docker volumes, execution of installation scripts,
 * and cleanup of temporary installation containers.
 */
class InstanceInstaller(
    private val dockerClient: DockerClient,
    private val kukenConfig: KukenConfig,
) {
    private val logger = LogManager.getLogger(InstanceInstaller::class.java)

    /**
     * Installs an instance by executing installation hooks if provided.
     *
     * If no installation hook is provided, creates a local installation directory.
     * Otherwise, creates a Docker volume, runs the installation script in a temporary
     * container, and returns the volume name.
     *
     * @param workingDir The working directory for the installation
     * @param instanceId The instance ID
     * @param onInstall The optional installation hook resource
     * @return The installation directory path or volume name
     */
    suspend fun install(
        workingDir: String,
        instanceId: Uuid,
        onInstall: AppResource?,
    ): String {
        if (onInstall == null) {
            logger.debug("Skipping instance installation, no install hook was set")
            val installDir = kukenConfig.engine.instancesDataDirectory.resolve(instanceId.toString())

            logger.debug("Installation directory: {}", installDir)
            Files.createDirectories(installDir)
            return installDir.pathString
        }

        logger.debug("Preparing installation...")
        logger.debug("Blueprint image working directory: $workingDir")

        var createdContainer: String? = null
        val volume =
            dockerClient.volumes.create {
                name = DOCKER_INSTALL_VOLUME_FORMAT.format(Random.nextUInt())
                labels = mapOf("gg.kuken.instance.id" to instanceId.toString())
            }

        try {
            dockerClient.images.pull(DOCKER_INSTALL_IMAGE).collect {}

            createdContainer =
                dockerClient.containers.create {
                    name = DOCKER_INSTALL_CONTAINER_FORMAT.format(Random.nextUInt())
                    image = DOCKER_INSTALL_IMAGE
                    entrypoint = listOf("tail", "-f", "/dev/null")
                    hostConfig {
                        binds = listOf("${volume.name}:$DEFAULT_DOCKER_INSTALL_WORKING_DIR")
                    }
                }
            dockerClient.containers.start(createdContainer)

            val installScriptFile = KukenConfig.tempDir(Paths.get(LOCAL_RESOURCES_DIR, onInstall.name)).toFile()
            logger.debug("Installation file: {}", installScriptFile)

            dockerClient.containers.copyDirectoryTo(
                container = createdContainer,
                sourcePath = installScriptFile.parentFile.absolutePath,
                destinationPath = "/tmp",
            )

            val makeInstallScriptExecutable =
                dockerClient.exec.create(createdContainer) {
                    command = listOf("chmod", "+x", "/tmp/${installScriptFile.name}")
                }
            dockerClient.exec.start(makeInstallScriptExecutable)

            val runScript =
                dockerClient.exec.start(
                    id =
                        dockerClient.exec.create(createdContainer) {
                            attachStdout = true
                            attachStderr = true
                            command = listOf("/tmp/${installScriptFile.name}")
                        },
                    options =
                        ExecStartOptions(
                            stream = true,
                            demux = false,
                        ),
                )

            require(runScript is ExecStartResult.Stream) {
                "Unexpected Docker exec start result type: ${runScript::class.qualifiedName}"
            }

            logger.debug("--- SCRIPT OUTPUT ---")
            runScript.output.collect { log ->
                logger.debug(log)
            }
            logger.debug("--- SCRIPT OUTPUT ---")

            val chmod =
                dockerClient.exec.create(createdContainer) {
                    command = listOf("chmod", "-R", "777", DEFAULT_DOCKER_INSTALL_WORKING_DIR)
                    attachStdout = true
                }
            dockerClient.exec.start(chmod)
        } finally {
            if (createdContainer != null) {
                dockerClient.containers.remove(createdContainer) { force = true }
            }
        }

        return volume.name
    }
}
