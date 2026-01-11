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
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
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
        logger.debug("Creating instance from {}", blueprintId)
        val blueprint = blueprintService.getBlueprint(blueprintId)
        val instanceId = identityGeneratorService.generate()
        val generatedName =
            generateContainerName(
                instanceId,
                blueprint.spec.build
                    ?.instance
                    ?.name,
            )
        logger.debug("Instance {} runtime identifier is {}", instanceId, generatedName)
        val mergedEnv =
            blueprint.spec.build
                ?.env
                .orEmpty() + options.env

        val createResult =
            tryGenerateRuntime(
                instanceId = instanceId,
                options = options.copy(env = mergedEnv),
                generatedName = generatedName,
            )

        logger.debug("Instance {} creation result: {}", instanceId, createResult)
        return handleInitialInstanceCreationResult(createResult, instanceId, blueprint)
    }

    private suspend fun tryGenerateRuntime(
        instanceId: Uuid,
        options: CreateInstanceOptions,
        generatedName: String,
    ): GenerateRuntimeResult {
        logger.debug("Generating runtime for instance {}", instanceId)
        val result =
            generateAndConnectRuntime(
                instanceId = instanceId,
                options = options,
                name = generatedName,
            )

        logger.debug("Instance {} runtime generated: {}", instanceId, result)
        when (result) {
            is GenerateRuntimeResult.Done -> {
                instanceRepository.update(instanceId) {
                    this.status = InstanceStatus.Created.label
                }
            }

            is GenerateRuntimeResult.MissingDockerImage -> {
                launch {
                    pullDockerImage(options.image).collect { pullStatus ->
                        logger.debug(
                            "Updating instance {} with new status due to Docker image pull: {}",
                            instanceId,
                            pullStatus.label,
                        )
                        instanceRepository.update(instanceId) {
                            this.status = pullStatus.label
                        }

                        if (pullStatus == InstanceStatus.ImagePullCompleted) {
                            tryGenerateRuntime(instanceId, options, generatedName)
                        }
                    }
                }
            }

            else -> {
                logger.error("Unexpected instance {} runtime generation result: {}", instanceId, result)
            }
        }

        return result
    }

    private suspend fun handleInitialInstanceCreationResult(
        result: GenerateRuntimeResult,
        instanceId: Uuid,
        blueprint: Blueprint,
    ): Instance =
        when (result) {
            is GenerateRuntimeResult.Done -> {
                registerInstance(
                    instanceId = instanceId,
                    blueprintId = blueprint.id,
                    status = InstanceStatus.Created,
                    containerId = result.runtimeIdentifier,
                    address = result.address,
                )
            }

            is GenerateRuntimeResult.RuntimeCreationFailed -> {
                registerInstance(
                    instanceId = instanceId,
                    blueprintId = blueprint.id,
                    status = InstanceStatus.Unavailable,
                    containerId = null,
                    address = result.address,
                )
            }

            is GenerateRuntimeResult.NetworkConnectFailed -> {
                registerInstance(
                    instanceId = instanceId,
                    blueprintId = blueprint.id,
                    status = InstanceStatus.NetworkAssignmentFailed,
                    containerId = result.runtimeIdentifier,
                    address = result.address,
                )
            }

            is GenerateRuntimeResult.MissingDockerImage -> {
                registerInstance(
                    instanceId = instanceId,
                    blueprintId = blueprint.id,
                    status = InstanceStatus.ImagePullNeeded,
                    containerId = null,
                    address = result.address,
                )
            }
        }

    private suspend fun generateAndConnectRuntime(
        instanceId: Uuid,
        name: String,
        options: CreateInstanceOptions,
    ): GenerateRuntimeResult {
        // TODO Allow random port assigned (options.port == null)
        //      https://github.com/DevNatan/docker-kotlin?tab=readme-ov-file#create-and-start-a-container-with-auto-assigned-port-bindings
        val address = dockerNetworkService.createAddress(options.host, options.port)

        val containerId =
            try {
                createRuntime(
                    instanceId = instanceId,
                    name = name,
                    options =
                        options.copy(
                            host = address.host,
                            port = address.port,
                        ),
                )
            } catch (_: ImageNotFoundException) {
                return GenerateRuntimeResult.MissingDockerImage(address)
            } catch (e: Throwable) {
                logger.error("Failed to create container for instance {}", instanceId, e)
                return GenerateRuntimeResult.RuntimeCreationFailed(address)
            }

        try {
            connectInstanceToNetwork(containerId = containerId)
        } catch (e: Throwable) {
            logger.error("Failed to connect container {} (instance: {}) to network", containerId, instanceId, e)
            return GenerateRuntimeResult.NetworkConnectFailed(containerId, address)
        }

        return GenerateRuntimeResult.Done(containerId, address)
    }

    private sealed class GenerateRuntimeResult {
        data class Done(
            val runtimeIdentifier: String,
            val address: HostPort,
        ) : GenerateRuntimeResult()

        data class RuntimeCreationFailed(
            val address: HostPort,
        ) : GenerateRuntimeResult()

        data class MissingDockerImage(
            val address: HostPort,
        ) : GenerateRuntimeResult()

        data class NetworkConnectFailed(
            val runtimeIdentifier: String,
            val address: HostPort,
        ) : GenerateRuntimeResult()
    }

    private fun pullDockerImage(image: String): Flow<InstanceStatus> =
        flow {
            logger.debug("Pulling image $image")
            try {
                dockerClient.images
                    .pull(image)
                    .onStart { emit(InstanceStatus.ImagePullInProgress) }
                    .onCompletion {
                        emit(InstanceStatus.ImagePullCompleted)
                        logger.debug("Image {} pull completed.", image)
                    }.collect { pull ->
                        logger.debug(
                            "{} {}: {}/{} ({})",
                            pull.statusText,
                            image,
                            pull.progressDetail?.current ?: "???",
                            pull.progressDetail?.total ?: "???",
                            pull.id,
                        )
                    }
            } catch (e: Throwable) {
                logger.error("Failed to pull image: {}", image, e)
                emit(InstanceStatus.ImagePullFailed)
            }
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

    private suspend fun createRuntime(
        instanceId: Uuid,
        name: String,
        options: CreateInstanceOptions,
    ): String {
        logger.debug("Creating runtime {} (image: {})...", instanceId, options.image)
        requireNotNull(options.port) { "Port cannot be null" }

        return dockerClient.containers.create {
            this@create.name = name
            this@create.image = options.image
            labels = mapOf("gg.kuken.instance.id" to instanceId.toString())
            env =
                options.env
                    .mapValues { (_, value) ->
                        value.replace("{addr.port}", options.port.toString())
                    }.map { (key, value) -> "$key=$value" }

            // TODO Allow random port assigned (options.port == null)
            //      https://github.com/DevNatan/docker-kotlin?tab=readme-ov-file#create-and-start-a-container-with-auto-assigned-port-bindings
            hostConfig {
                portBindings(options.port) {
                    add(PortBinding(options.host, options.port))
                }
            }
        }
    }

    private suspend fun connectInstanceToNetwork(containerId: String) {
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
                        destination = mount.target,
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
