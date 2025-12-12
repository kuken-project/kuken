package gg.kuken.feature.instance

import gg.kuken.KukenConfig
import gg.kuken.feature.account.IdentityGeneratorService
import gg.kuken.feature.blueprint.BlueprintService
import gg.kuken.feature.blueprint.model.Blueprint
import gg.kuken.feature.instance.entity.InstanceEntity
import gg.kuken.feature.instance.model.CreateInstanceOptions
import gg.kuken.feature.instance.model.HostPort
import gg.kuken.feature.instance.model.ImageUpdatePolicy
import gg.kuken.feature.instance.model.Instance
import gg.kuken.feature.instance.model.InstanceRuntime
import gg.kuken.feature.instance.model.InstanceRuntimeMount
import gg.kuken.feature.instance.model.InstanceRuntimeNetwork
import gg.kuken.feature.instance.model.InstanceRuntimeSingleNetwork
import gg.kuken.feature.instance.model.InstanceStatus
import gg.kuken.feature.instance.repository.InstanceRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import me.devnatan.dockerkt.DockerClient
import me.devnatan.dockerkt.models.PortBinding
import me.devnatan.dockerkt.models.container.hostConfig
import me.devnatan.dockerkt.models.portBindings
import me.devnatan.dockerkt.resource.container.create
import me.devnatan.dockerkt.resource.container.remove
import me.devnatan.dockerkt.resource.exec.create
import me.devnatan.dockerkt.resource.exec.start
import me.devnatan.dockerkt.resource.image.ImageNotFoundException
import org.apache.logging.log4j.LogManager
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

class DockerInstanceService(
    private val dockerClient: DockerClient,
    private val instanceRepository: InstanceRepository,
    private val blueprintService: BlueprintService,
    private val identityGeneratorService: IdentityGeneratorService,
    private val kukenConfig: KukenConfig,
) : InstanceService {
    private val logger = LogManager.getLogger(DockerInstanceService::class.java)
    private val dockerNetworkService = DockerNetworkService(dockerClient)

    override suspend fun getInstance(instanceId: Uuid): Instance {
        val instance = instanceRepository.findById(instanceId) ?: throw InstanceNotFoundException()

        return Instance(
            id = instance.id.value.toKotlinUuid(),
            status = instance.status.let(InstanceStatus::valueOf),
            containerId = instance.containerId,
            updatePolicy = instance.updatePolicy.let(ImageUpdatePolicy::getById),
            address = HostPort(instance.host!!, instance.port!!),
            runtime = null,
            blueprintId = instance.blueprintId.toKotlinUuid(),
            createdAt = instance.createdAt,
            nodeId = instance.nodeId,
        )
    }

    override suspend fun createInstance(
        blueprintId: Uuid,
        options: CreateInstanceOptions,
    ): Instance {
        val blueprint = blueprintService.getBlueprint(blueprintId)
        val instanceId = identityGeneratorService.generate()
        val generatedName =
            generateContainerName(
                instanceId = instanceId,
                nameFormat =
                    blueprint.spec.build
                        ?.instance
                        ?.name,
            )

        return try {
            createAndRegisterInstance(instanceId, options, generatedName, blueprint)
        } catch (_: ImageNotFoundException) {
            registerInstance(
                instanceId = instanceId,
                blueprintId = blueprint.id,
                status = InstanceStatus.ImagePullNeeded,
                containerId = null,
                address = null,
            )

            pullImageAndCreateInstance(
                instanceId = instanceId,
                instanceName = generatedName,
                options = options,
            )
        } catch (e: Throwable) {
            throw InstanceCreationException("Failed to create instance", e)
        }
    }

    /**
     * Creates and registers a new instance based on the provided parameters.
     * Initializes a container by creating and connecting it, then registers the instance with
     * the appropriate status and associated details.
     *
     * @param instanceId The unique identifier for the instance to be created.
     * @param options The options specifying the image, host, and port to be used for the instance.
     * @param generatedName The generated name used to identify the instance's container.
     * @param blueprint The blueprint specifying the configuration details for the instance.
     * @return The newly created and registered instance.
     */
    private suspend fun createAndRegisterInstance(
        instanceId: Uuid,
        options: CreateInstanceOptions,
        generatedName: String,
        blueprint: Blueprint,
    ): Instance {
        val env =
            blueprint.spec.build
                ?.env
                .orEmpty() + options.env
        val (containerId, address) =
            createAndConnectContainer(
                instanceId = instanceId,
                options =
                    options.copy(
                        env = env,
                    ),
                name = generatedName,
            )

        val status =
            if (address == null) {
                InstanceStatus.NetworkAssignmentFailed
            } else {
                InstanceStatus.Created
            }

        return registerInstance(
            instanceId = instanceId,
            blueprintId = blueprint.id,
            status = status,
            containerId = containerId,
            address = address,
        )
    }

    /**
     * Creates a container for the specified instance and connects it to the configured network.
     *
     * @param instanceId The unique identifier of the instance for which the container is being created.
     * @param name The name to assign to the created container.
     * @return A pair containing the ID of the created container and the associated `HostPort` information,
     *         or null for `HostPort` if the connection to the network failed.
     */
    private suspend fun createAndConnectContainer(
        instanceId: Uuid,
        name: String,
        options: CreateInstanceOptions,
    ): Pair<String, HostPort?> {
        val address = dockerNetworkService.createAddress(options.host, options.port)
        val containerId =
            createContainer(
                instanceId = instanceId,
                name = name,
                options =
                    options.copy(
                        host = address.host,
                        port = address.port,
                    ),
            )

        connectInstance(containerId = containerId)
        return containerId to address
    }

    /**
     * Pulls the specified container image and creates an instance using the pulled image.
     * Updates the instance repository with the status of the operation and connects the created container
     * to the desired network. Handles image pull completion or failure and sets the appropriate status.
     *
     * @param instanceId The unique identifier of the instance being created.
     * @param instanceName The name to assign to the instance's container.
     * @param options The options containing the image, host, and port details for creating the instance.
     * @return The created instance object containing status, container details, and metadata.
     * @throws InstanceNotFoundException If the instance with the given ID cannot be found during the update.
     */
    private suspend fun pullImageAndCreateInstance(
        instanceId: Uuid,
        instanceName: String,
        options: CreateInstanceOptions,
    ): Instance {
        val job = CompletableDeferred<InstanceStatus>()

        dockerClient.images
            .pull(options.image)
            .catch { error -> logger.error("Failed to pull image: {}", options.image, error) }
            .onCompletion { error ->
                val status =
                    if (error != null) {
                        InstanceStatus.ImagePullFailed
                    } else {
                        InstanceStatus.ImagePullCompleted
                    }

                logger.debug("Image {} pull completed.", options.image)
                job.complete(status)
            }.collect { pull -> logger.debug("Pulling image {}: {}", options.image, pull) }

        val (container, address) =
            createAndConnectContainer(
                instanceId = instanceId,
                name = instanceName,
                options = options,
            )

        val imagePullStatus = job.await()
        val updatedInstance =
            instanceRepository.update(instanceId) {
                this.containerId = container
                this.host = address?.host
                this.port = address?.port
                this.status =
                    when {
                        address == null -> InstanceStatus.NetworkAssignmentFailed
                        else -> imagePullStatus
                    }.label
            } ?: throw InstanceNotFoundException()

        return toInstance(updatedInstance)
    }

    private suspend fun toInstance(instance: InstanceEntity): Instance =
        Instance(
            id = instance.id.value.toKotlinUuid(),
            status = InstanceStatus.getByLabel(instance.status),
            updatePolicy = ImageUpdatePolicy.getById(instance.updatePolicy),
            containerId = instance.containerId,
            address = HostPort(host = instance.host, port = instance.port!!),
            runtime = buildRuntime(instance.containerId!!),
            blueprintId = instance.blueprintId.toKotlinUuid(),
            createdAt = instance.createdAt,
            nodeId = instance.nodeId,
        )

    private suspend fun createContainer(
        instanceId: Uuid,
        name: String,
        options: CreateInstanceOptions,
    ): String {
        logger.debug("Creating container with {} to {}...", options.image, instanceId)
        requireNotNull(options.port)

        return dockerClient.containers.create {
            this@create.name = name
            this@create.image = options.image
            labels = mapOf("gg.kuken.instance.id" to instanceId.toString())
            env =
                options.env
                    .mapValues { (_, value) ->
                        value.replace("{addr.port}", options.port.toString())
                    }.map { (key, value) -> "$key=$value" }

            hostConfig {
                portBindings(options.port) {
                    add(PortBinding(options.host, options.port))
                }
            }
        }
    }

    private suspend fun connectInstance(containerId: String) {
        val networkToConnect = kukenConfig.docker.network.name
        logger.debug("Connecting $containerId to $networkToConnect...")

        runCatching {
            dockerNetworkService.connect(
                network = networkToConnect,
                container = containerId,
            )
            logger.debug("Connected {} to {}", containerId, networkToConnect)
        }.onFailure { error ->
            logger.error("Unable to connect {} to the network {}", containerId, networkToConnect, error)
        }.getOrNull()
    }

    private suspend fun registerInstance(
        instanceId: Uuid,
        blueprintId: Uuid,
        status: InstanceStatus,
        containerId: String?,
        address: HostPort?,
    ): Instance {
        val runtime = if (containerId == null) null else buildRuntime(containerId)
        val instance =
            Instance(
                id = instanceId,
                status = status,
                updatePolicy = ImageUpdatePolicy.Always,
                containerId = containerId,
                address = address,
                runtime = runtime,
                blueprintId = blueprintId,
                createdAt = Clock.System.now(),
                nodeId = kukenConfig.node,
            )

        instanceRepository.create(instance)
        return instance
    }

    private suspend fun buildRuntime(containerId: String): InstanceRuntime {
        val inspection = dockerClient.containers.inspect(containerId)
        val networkSettings = inspection.networkSettings
        val state = inspection.state

        return InstanceRuntime(
            id = inspection.id,
            network =
                InstanceRuntimeNetwork(
                    ipV4Address = networkSettings.ipAddress!!,
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
                        destination = mount.target!!,
                        readonly = mount.readonly,
                    )
                },
        )
    }

    private fun generateContainerName(
        instanceId: Uuid,
        nameFormat: String?,
    ): String {
        val format = nameFormat ?: "kuken-{node}-{id}"

        return format
            .replace("{id}", instanceId.toString())
            .replace("{node}", kukenConfig.node)
    }

    suspend fun getValidInstance(instanceId: Uuid): InstanceEntity {
        val instance = instanceRepository.findById(instanceId) ?: throw InstanceNotFoundException()
        if (instance.containerId == null) {
            throw InstanceUnreachableRuntimeException()
        }

        return instance
    }

    override suspend fun deleteInstance(instanceId: Uuid) {
        val instance = instanceRepository.delete(instanceId) ?: throw InstanceNotFoundException()
        val containerId = instance.containerId ?: throw InstanceUnreachableRuntimeException()

        dockerClient.containers.remove(container = containerId) {
            force = true
            removeAnonymousVolumes = true
        }
    }

    override suspend fun startInstance(instanceId: Uuid) {
        val instance = getValidInstance(instanceId)
        dockerClient.containers.start(instance.containerId!!)
    }

    override suspend fun stopInstance(instanceId: Uuid) {
        val instance = getValidInstance(instanceId)
        dockerClient.containers.stop(instance.containerId!!)
    }

    override suspend fun runInstanceCommand(
        instanceId: Uuid,
        commandToRun: String,
    ) {
        val instance = getValidInstance(instanceId)
        val execId =
            dockerClient.exec.create(instance.containerId!!) {
                tty = true
                attachStdin = false
                attachStdout = false
                attachStderr = false
                command = commandToRun.split(" ")
            }

        dockerClient.exec.start(execId) {
            detach = true
        }
    }
}
