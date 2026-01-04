package gg.kuken.feature.instance

import gg.kuken.KukenConfig
import gg.kuken.core.EventDispatcher
import gg.kuken.core.docker.DockerNetworkService
import gg.kuken.feature.account.IdentityGeneratorService
import gg.kuken.feature.blueprint.BlueprintService
import gg.kuken.feature.blueprint.model.Blueprint
import gg.kuken.feature.instance.data.entity.InstanceEntity
import gg.kuken.feature.instance.data.repository.InstanceRepository
import gg.kuken.feature.instance.model.CreateInstanceOptions
import gg.kuken.feature.instance.model.HostPort
import gg.kuken.feature.instance.model.ImageUpdatePolicy
import gg.kuken.feature.instance.model.Instance
import gg.kuken.feature.instance.model.InstanceRuntime
import gg.kuken.feature.instance.model.InstanceRuntimeMount
import gg.kuken.feature.instance.model.InstanceRuntimeNetwork
import gg.kuken.feature.instance.model.InstanceRuntimeSingleNetwork
import gg.kuken.feature.instance.model.InstanceStatus
import gg.kuken.http.exception.ResourceException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import me.devnatan.dockerkt.DockerClient
import me.devnatan.dockerkt.models.PortBinding
import me.devnatan.dockerkt.models.container.hostConfig
import me.devnatan.dockerkt.models.portBindings
import me.devnatan.dockerkt.resource.container.ContainerNotFoundException
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
    private val dockerNetworkService: DockerNetworkService,
    private val eventDispatcher: EventDispatcher,
) : InstanceService,
    CoroutineScope by CoroutineScope(Default + CoroutineName("DockerInstanceService")) {
    private val logger = LogManager.getLogger(DockerInstanceService::class.java)

    override suspend fun getInstance(instanceId: Uuid): Instance {
        val instance = instanceRepository.findById(instanceId) ?: throw InstanceNotFoundException()
        val runtime = instance.containerId?.let { id -> tryBuildRuntime(id) }

        return Instance(
            id = instance.id.value.toKotlinUuid(),
            status = instance.status.let(InstanceStatus::getByLabel),
            containerId = instance.containerId,
            updatePolicy = instance.updatePolicy.let(ImageUpdatePolicy::getById),
            address = HostPort(host = instance.host, port = instance.port!!),
            runtime = runtime,
            blueprintId = instance.blueprintId.toKotlinUuid(),
            createdAt = instance.createdAt,
            nodeId = instance.nodeId,
        )
    }

    override suspend fun getInstanceNoRuntime(instanceId: Uuid): Instance {
        val instance = instanceRepository.findById(instanceId) ?: throw InstanceNotFoundException()

        return Instance(
            id = instance.id.value.toKotlinUuid(),
            status = instance.status.let(InstanceStatus::getByLabel),
            containerId = instance.containerId,
            updatePolicy = instance.updatePolicy.let(ImageUpdatePolicy::getById),
            address = HostPort(host = instance.host, port = instance.port!!),
            runtime = null,
            blueprintId = instance.blueprintId.toKotlinUuid(),
            createdAt = instance.createdAt,
            nodeId = instance.nodeId,
        )
    }

    override suspend fun getInstanceContainerId(instanceId: Uuid): String {
        val containerId = instanceRepository.findContainerById(instanceId) ?: throw InstanceNotFoundException()
        return containerId
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

        val result =
            createAndConnectContainer(
                instanceId = instanceId,
                options = options.copy(env = env),
                name = generatedName,
            )

        return handleCreateAndContainerResult(result, instanceId, blueprint)
    }

    private suspend fun handleCreateAndContainerResult(
        result: CreateAndConnectContainerResult,
        instanceId: Uuid,
        blueprint: Blueprint,
    ): Instance =
        when (result) {
            is CreateAndConnectContainerResult.Done -> {
                registerInstance(
                    instanceId = instanceId,
                    blueprintId = blueprint.id,
                    status = InstanceStatus.Created,
                    containerId = result.containerId,
                    address = result.address,
                )
            }

            is CreateAndConnectContainerResult.CreateContainerFailed -> {
                registerInstance(
                    instanceId = instanceId,
                    blueprintId = blueprint.id,
                    status = InstanceStatus.Unavailable,
                    containerId = null,
                    address = result.address,
                )
            }

            is CreateAndConnectContainerResult.NetworkConnectFailed -> {
                registerInstance(
                    instanceId = instanceId,
                    blueprintId = blueprint.id,
                    status = InstanceStatus.NetworkAssignmentFailed,
                    containerId = result.containerId,
                    address = result.address,
                )
            }
        }

    private suspend fun createAndConnectContainer(
        instanceId: Uuid,
        name: String,
        options: CreateInstanceOptions,
    ): CreateAndConnectContainerResult {
        val address = dockerNetworkService.createAddress(options.host, options.port)
        val containerId: String

        try {
            containerId =
                createContainer(
                    instanceId = instanceId,
                    name = name,
                    options =
                        options.copy(
                            host = address.host,
                            port = address.port,
                        ),
                )
        } catch (e: Throwable) {
            logger.error("Failed to create container for instance {}", instanceId, e)
            return CreateAndConnectContainerResult.CreateContainerFailed(address)
        }

        try {
            connectInstance(containerId = containerId)
        } catch (e: Throwable) {
            logger.error("Failed to connect container {} (instance: {}) to network", containerId, instanceId, e)
            return CreateAndConnectContainerResult.NetworkConnectFailed(containerId, address)
        }

        return CreateAndConnectContainerResult.Done(containerId, address)
    }

    private sealed class CreateAndConnectContainerResult {
        data class Done(
            val containerId: String,
            val address: HostPort,
        ) : CreateAndConnectContainerResult()

        data class CreateContainerFailed(
            val address: HostPort,
        ) : CreateAndConnectContainerResult()

        data class NetworkConnectFailed(
            val containerId: String,
            val address: HostPort,
        ) : CreateAndConnectContainerResult()
    }

    private suspend fun pullImageForInstanceCreation(image: String): InstanceStatus {
        try {
            dockerClient.images
                .pull(image)
                .collect { pull -> logger.debug("Pulling image: {}: {}", image, pull) }
        } catch (exception: ResourceException) {
            logger.error("Failed to pull image: {}", image, exception)
            return InstanceStatus.ImagePullFailed
        }

        logger.debug("Image {} pull completed.", image)
        return InstanceStatus.ImagePullCompleted
    }

    private suspend fun pullImageAndCreateInstance(
        instanceId: Uuid,
        instanceName: String,
        options: CreateInstanceOptions,
    ): Instance {
        val instanceImageStatus = pullImageForInstanceCreation(options.image)
        val createResult =
            createAndConnectContainer(
                instanceId = instanceId,
                name = instanceName,
                options = options,
            )

        val updatedInstance =
            when (createResult) {
                is CreateAndConnectContainerResult.Done -> {
                    val container = createResult.containerId
                    val address = createResult.address

                    instanceRepository.update(instanceId) {
                        this.containerId = container
                        this.host = address.host
                        this.port = address.port
                        this.status = InstanceStatus.Created.label
                    }
                }

                is CreateAndConnectContainerResult.CreateContainerFailed -> {
                    val address = createResult.address

                    instanceRepository.update(instanceId) {
                        this.host = address.host
                        this.port = address.port
                        this.status = instanceImageStatus.label
                    }
                }

                is CreateAndConnectContainerResult.NetworkConnectFailed -> {
                    val container = createResult.containerId
                    val address = createResult.address

                    instanceRepository.update(instanceId) {
                        this.containerId = container
                        this.host = address.host
                        this.port = address.port
                        this.status = InstanceStatus.NetworkAssignmentFailed.label
                    }
                }
            }

        if (updatedInstance == null) {
            throw InstanceNotFoundException()
        }

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
        dockerNetworkService.connect(
            network = networkToConnect,
            container = containerId,
        )
        logger.debug("Connected {} to {}", containerId, networkToConnect)
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

    private suspend fun tryBuildRuntime(containerId: String): InstanceRuntime? =
        try {
            buildRuntime(containerId)
        } catch (_: ContainerNotFoundException) {
            null
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
        val format = nameFormat ?: "kk-{node}-{id}"

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
        dockerClient.containers.start(getInstanceContainerId(instanceId))
    }

    override suspend fun stopInstance(instanceId: Uuid) {
        dockerClient.containers.stop(getInstanceContainerId(instanceId))
    }

    override suspend fun runInstanceCommand(
        instanceId: Uuid,
        commandToRun: String,
    ): Int {
        val container = getInstanceContainerId(instanceId)
        val execId =
            dockerClient.exec.create(container) {
                tty = true
                attachStdin = false
                attachStdout = false
                attachStderr = false
                command = listOf("sh", "-c") + commandToRun
            }

        dockerClient.exec.start(execId) {
            detach = true
        }

        return dockerClient.exec.inspect(execId).exitCode
    }
}
