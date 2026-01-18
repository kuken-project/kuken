package gg.kuken.feature.instance

import gg.kuken.KukenConfig
import gg.kuken.core.EventDispatcher
import gg.kuken.core.docker.DockerNetworkService
import gg.kuken.feature.account.IdentityGeneratorService
import gg.kuken.feature.blueprint.BlueprintService
import gg.kuken.feature.blueprint.model.Blueprint
import gg.kuken.feature.blueprint.processor.AppResource
import gg.kuken.feature.blueprint.processor.BlueprintResolutionContext
import gg.kuken.feature.blueprint.processor.CheckboxInput
import gg.kuken.feature.blueprint.processor.DataSizeInput
import gg.kuken.feature.blueprint.processor.InstanceSettingsCommandExecutor
import gg.kuken.feature.blueprint.processor.PasswordInput
import gg.kuken.feature.blueprint.processor.PortInput
import gg.kuken.feature.blueprint.processor.Resolvable
import gg.kuken.feature.blueprint.processor.ResolvedBlueprint
import gg.kuken.feature.blueprint.processor.SelectInput
import gg.kuken.feature.blueprint.processor.TextInput
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import me.devnatan.dockerkt.DockerClient
import me.devnatan.dockerkt.models.PortBinding
import me.devnatan.dockerkt.models.container.hostConfig
import me.devnatan.dockerkt.models.exec.ExecStartOptions
import me.devnatan.dockerkt.models.exec.ExecStartResult
import me.devnatan.dockerkt.models.portBindings
import me.devnatan.dockerkt.resource.container.ContainerNotFoundException
import me.devnatan.dockerkt.resource.container.create
import me.devnatan.dockerkt.resource.container.remove
import me.devnatan.dockerkt.resource.exec.create
import me.devnatan.dockerkt.resource.exec.start
import me.devnatan.dockerkt.resource.image.ImageNotFoundException
import me.devnatan.dockerkt.resource.volume.create
import org.apache.logging.log4j.LogManager
import java.io.File
import kotlin.random.Random
import kotlin.random.nextUInt
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
    companion object {
        private val logger = LogManager.getLogger(DockerInstanceService::class.java)

        @OptIn(InternalSerializationApi::class)
        fun performSubstitutions(
            property: Resolvable<*>,
            blueprint: ResolvedBlueprint,
            resolutionContext: BlueprintResolutionContext,
        ): String? =
            when (property) {
                is Resolvable.EnvVarRef -> {
                    resolutionContext.env[property.envVarName].orEmpty()
                }

                is Resolvable.InputRef -> {
                    val definition = blueprint.inputs.firstOrNull { it.name == property.inputName }
                    val defaultValue: String? =
                        when (definition) {
                            is CheckboxInput -> {
                                performSubstitutions(
                                    property = definition.default,
                                    blueprint = blueprint,
                                    resolutionContext = resolutionContext,
                                ) ?: "false"
                            }

                            is DataSizeInput -> {
                                null
                            }

                            is PasswordInput -> {
                                null
                            }

                            is PortInput -> {
                                performSubstitutions(definition.default, blueprint, resolutionContext)
                            }

                            is SelectInput -> {
                                null
                            }

                            is TextInput -> {
                                null
                            }

                            null -> {
                                null
                            }
                        }

                    val input =
                        resolutionContext.inputs[property.inputName]
                            ?: defaultValue
                            ?: error("Missing required input ${property.inputName}")

                    input
                }

                is Resolvable.Interpolated -> {
                    logger.debug("Resolving dynamic property...: ${property.template}")

                    check(property.parts.isNotEmpty()) {
                        "Dynamic property provided but no dependencies found"
                    }

                    var literal = property.template
                    property.parts
                        .groupBy { it.toTemplateString() }
                        .forEach { (template, dependencies) ->
                            check(dependencies.size == 1) {
                                "Multiple dependencies are not supported"
                            }

                            for (dependency in dependencies) {
                                val substitution =
                                    performSubstitutions(dependency, blueprint, resolutionContext)
                                        ?: continue

                                literal = literal.replace(template, substitution)
                            }
                        }

                    literal
                }

                is Resolvable.Literal -> {
                    if (property.value == "null") null else property.value
                }

                is Resolvable.RuntimeRef -> {
                    null
                }

                Resolvable.Null -> {
                    null
                }
            }
    }

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

    override suspend fun createInstance(options: CreateInstanceOptions): Instance {
        val blueprintId = options.blueprint

        logger.debug("Creating instance from {}: {}", blueprintId, options)
        val blueprint = blueprintService.getBlueprint(blueprintId)
        val resolutionContext =
            BlueprintResolutionContext(
                inputs = options.inputs,
                env = options.env,
            )

        val env =
            blueprint.spec.build.environmentVariables
                .map { env -> env.name to performSubstitutions(env.value, blueprint.spec, resolutionContext) }
                .filter { (_, value) -> value != null }
                .associate { (key, value) -> key to value!! }

        val options = options.copy(env = env)

        val instanceId = identityGeneratorService.generate()
        val generatedName =
            generateContainerName(
                instanceId,
                "kk-{id}",
            )
        logger.debug("Instance {} runtime identifier is {}", instanceId, generatedName)

        val image =
            requireNotNull(
                performSubstitutions(blueprint.spec.build.docker.image, blueprint.spec, resolutionContext),
            ) {
                "Docker image cannot be null"
            }

        val createResult =
            tryGenerateRuntime(
                instanceId = instanceId,
                options = options,
                image = image,
                generatedName = generatedName,
                onInstall = blueprint.spec.hooks.onInstall,
            )

        logger.debug("Instance {} creation result: {}", instanceId, createResult)
        return handleInitialInstanceCreationResult(createResult, instanceId, blueprint)
    }

    private suspend fun tryGenerateRuntime(
        instanceId: Uuid,
        image: String,
        generatedName: String,
        options: CreateInstanceOptions,
        onInstall: AppResource?,
    ): GenerateRuntimeResult {
        logger.debug("Generating runtime for instance {}", instanceId)
        val result =
            generateAndConnectRuntime(
                instanceId = instanceId,
                options = options,
                image = image,
                name = generatedName,
                onInstall = onInstall,
            )

        logger.debug("Instance {} runtime generated: {}", instanceId, result)
        when (result) {
            is GenerateRuntimeResult.Done -> {
                instanceRepository.update(instanceId) {
                    this.status = InstanceStatus.Created.label
                    this.containerId = result.runtimeIdentifier
                }
            }

            is GenerateRuntimeResult.MissingDockerImage -> {
                launch {
                    pullDockerImage(image).collect { pullStatus ->
                        logger.debug(
                            "Updating instance {} with new status due to Docker image pull: {}",
                            instanceId,
                            pullStatus.label,
                        )
                        instanceRepository.update(instanceId) {
                            this.status = pullStatus.label
                        }

                        if (pullStatus == InstanceStatus.ImagePullCompleted) {
                            tryGenerateRuntime(instanceId, image, generatedName, options, onInstall)
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
        image: String,
        options: CreateInstanceOptions,
        onInstall: AppResource?,
    ): GenerateRuntimeResult {
        // TODO Allow random port assigned (options.port == null)
        //      https://github.com/DevNatan/docker-kotlin?tab=readme-ov-file#create-and-start-a-container-with-auto-assigned-port-bindings
        val address = dockerNetworkService.createAddress(host = options.address.host, port = options.address.port)

        val containerId =
            try {
                createRuntime(
                    instanceId = instanceId,
                    name = name,
                    image = image,
                    options = options,
                    onInstall = onInstall,
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
            dockerClient.images
                .pull(image)
                .onStart {
                    logger.debug("Pulling image $image")
                    emit(InstanceStatus.ImagePullInProgress)
                }.catch { exception ->
                    logger.error("Failed to pull image: {}", image, exception)
                    emit(InstanceStatus.ImagePullFailed)
                }.onCompletion {
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

    private suspend fun installInstance(
        workingDir: String,
        onInstall: AppResource?,
    ): String {
        if (onInstall == null) {
            logger.debug("Skipping instance installation, no install hook was set")
            return "/home/container"
        }

        val resourcesDir = File(".kuken/resources")

        logger.debug("Preparing installation...")
        val file = File(resourcesDir, onInstall.name)

        logger.debug("Installation file: ${file.absolutePath}")

        var createdContainer: String? = null

        println("Blueprint image working directory: $workingDir")

        val volume =
            dockerClient.volumes.create {
                name = "kuken-volume-${Random.nextUInt()}"
            }

        try {
            dockerClient.images.pull("alpine:latest").collect()

            createdContainer =
                dockerClient.containers.create {
                    name = "kuken-installer-${Random.nextUInt()}"
                    image = "alpine:latest"
                    entrypoint = listOf("tail", "-f", "/dev/null")
                    hostConfig {
                        binds = listOf("${volume.name}:/mnt/server")
                    }
                }
            dockerClient.containers.start(createdContainer)

            dockerClient.containers.copyDirectoryTo(
                container = createdContainer,
                sourcePath = file.parentFile.absolutePath,
                destinationPath = "/tmp",
            )

            val makeExecutable =
                dockerClient.exec.create(createdContainer) {
                    command = listOf("chmod", "+x", "/tmp/${file.name}")
                }
            dockerClient.exec.start(makeExecutable)

            val execute =
                dockerClient.exec.create(createdContainer) {
                    attachStdout = true
                    attachStderr = true
                    command = listOf("/tmp/${file.name}")
                }
            val result =
                dockerClient.exec.start(
                    execute,
                    ExecStartOptions(
                        stream = true,
                        demux = false,
                    ),
                )

            logger.debug("Exec result: {}", result)
            require(result is ExecStartResult.Stream)

            logger.debug("--- SCRIPT OUTPUT ---")
            result.output.collect {
                println(it)
            }
            logger.debug("--- SCRIPT OUTPUT ---")
        } finally {
            if (createdContainer != null) {
                dockerClient.containers.remove(createdContainer) { force = true }
            }
        }

        return volume.name
    }

    private suspend fun createRuntime(
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
        val installationVolume = installInstance(workingDir, onInstall)

        val containerId =
            dockerClient.containers.create {
                this@create.name = name
                this@create.image = image

                labels = mapOf("gg.kuken.instance.id" to instanceId.toString())

                // TODO Assign and replace reference values
                env =
                    options.env
                        .mapValues { (_, value) ->
                            value.replace("{addr.port}", options.address.port.toString())
                        }.map { (key, value) -> "$key=$value" }

                // TODO Allow random port assigned (options.port == null)
                //      https://github.com/DevNatan/docker-kotlin?tab=readme-ov-file#create-and-start-a-container-with-auto-assigned-port-bindings
                hostConfig {
                    portBindings(options.address.port) {
                        add(PortBinding(options.address.host, options.address.port))
                    }

                    binds = listOf("$installationVolume:$workingDir")
                }
            }

        dockerClient.containers.start(containerId)

        val permissions =
            dockerClient.exec.create(containerId) {
                command = listOf("chmod", "-R", "777", workingDir)
                attachStdout = true
            }

        val result = dockerClient.exec.start(permissions)

        logger.debug("CHMOD result: {}", result)

        return containerId
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
    ): Int? {
        val instance = getInstance(instanceId)
        val blueprint = blueprintService.getBlueprint(instance.blueprintId)

        return when (val commandExecutor = blueprint.spec.instanceSettings?.commandExecutor) {
            is InstanceSettingsCommandExecutor.SSH -> {
                val template = String.format(commandExecutor.template, commandToRun)
                logger.debug("Sending command to {}: {}", instance.id, template)

                val execId =
                    dockerClient.exec.create(instance.containerId!!) {
                        tty = true
                        attachStdin = false
                        attachStdout = false
                        attachStderr = false
                        user = "1000"
                        command = template.split(" ")
                    }

                dockerClient.exec.start(execId) {
                    detach = true
                }

                dockerClient.exec.inspect(execId).exitCode
            }

            is InstanceSettingsCommandExecutor.Rcon -> {
                error("RCON Command executor not supported")
            }

            null -> {
                null
            }
        }
    }
}
