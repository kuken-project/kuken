package gg.kuken.feature.instance

import gg.kuken.KukenConfig
import gg.kuken.core.ResourceId
import gg.kuken.core.docker.DockerContainerService
import gg.kuken.core.docker.DockerImageService
import gg.kuken.core.docker.DockerNetworkService
import gg.kuken.feature.account.IdentityGeneratorService
import gg.kuken.feature.blueprint.BlueprintPropertyResolver
import gg.kuken.feature.blueprint.BlueprintSpecProvider
import gg.kuken.feature.blueprint.processor.AppResource
import gg.kuken.feature.blueprint.processor.BlueprintProcessor
import gg.kuken.feature.blueprint.processor.BlueprintResolutionContext
import gg.kuken.feature.blueprint.processor.InstanceBlueprintResourceReader
import gg.kuken.feature.blueprint.processor.ResolvedBlueprintRefs
import gg.kuken.feature.blueprint.service.BlueprintService
import gg.kuken.feature.instance.data.entity.InstanceEntity
import gg.kuken.feature.instance.data.repository.InstanceRepository
import gg.kuken.feature.instance.model.CreateInstanceOptions
import gg.kuken.feature.instance.model.DockerImagePullStatus
import gg.kuken.feature.instance.model.HostPort
import gg.kuken.feature.instance.model.ImageUpdatePolicy
import gg.kuken.feature.instance.model.Instance
import gg.kuken.feature.instance.model.InstanceStatus
import gg.kuken.feature.instance.service.InstanceCommandExecutor
import gg.kuken.feature.instance.service.InstanceRuntimeBuilder
import gg.kuken.feature.instance.util.FramePersistentIdGenerator
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import me.devnatan.dockerkt.resource.image.ImageNotFoundException
import org.apache.logging.log4j.LogManager
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

class DockerInstanceService(
    private val instanceRepository: InstanceRepository,
    private val blueprintService: BlueprintService,
    private val identityGeneratorService: IdentityGeneratorService,
    private val kukenConfig: KukenConfig,
    private val dockerNetworkService: DockerNetworkService,
    private val blueprintSpecProvider: BlueprintSpecProvider,
    private val blueprintProcessor: BlueprintProcessor,
    private val activityLogStore: ActivityLogStore,
    private val blueprintPropertyResolver: BlueprintPropertyResolver,
    private val instanceRuntimeBuilder: InstanceRuntimeBuilder,
    private val dockerContainerService: DockerContainerService,
    private val dockerImageService: DockerImageService,
    private val instanceCommandExecutor: InstanceCommandExecutor,
) : InstanceService,
    CoroutineScope by CoroutineScope(Default + CoroutineName("DockerInstanceService")) {
    companion object {
        private val logger = LogManager.getLogger(DockerInstanceService::class.java)
    }

    override suspend fun getInstance(instanceId: Uuid): Instance {
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

    override suspend fun getInstanceWithRuntime(instanceId: Uuid): Instance {
        val instance = instanceRepository.findById(instanceId) ?: throw InstanceNotFoundException()
        val runtime = instance.containerId?.let { id -> instanceRuntimeBuilder.tryBuildRuntime(id) }

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

    override suspend fun getInstanceContainerId(instanceId: Uuid): String {
        val containerId = instanceRepository.findContainerById(instanceId) ?: throw InstanceNotFoundException()
        return containerId
    }

    override suspend fun createInstance(options: CreateInstanceOptions): Instance {
        val blueprintId = options.blueprint

        logger.debug("Creating instance from {}: {}", blueprintId, options)
        val blueprint = blueprintService.getBlueprint(blueprintId)
        val instanceId = identityGeneratorService.generate()
        val generatedName = generateContainerName(instanceId, "kk-{id}")

        val resolutionContext =
            BlueprintResolutionContext(
                instanceId = instanceId,
                instanceName = generatedName,
                inputs = options.inputs,
                env = options.env,
                address = options.address,
            )

        val resourceReader =
            InstanceBlueprintResourceReader(
                inputValues = options.inputs,
                refProvider = { key ->
                    when (key) {
                        ResolvedBlueprintRefs.INSTANCE_ID -> instanceId
                        ResolvedBlueprintRefs.INSTANCE_NAME -> generatedName
                        ResolvedBlueprintRefs.NETWORK_HOST -> options.address.host
                        ResolvedBlueprintRefs.NETWORK_PORT -> options.address.port.toString()
                    }
                },
            )
        val processedBlueprint =
            blueprintProcessor.process(
                input = blueprintSpecProvider.provide(blueprint.origin),
                readers = listOf(resourceReader),
            )

        val env =
            processedBlueprint.build.environmentVariables
                .map { env -> env.name to blueprintPropertyResolver.resolve(env.value, processedBlueprint, resolutionContext) }
                .filter { (_, value) -> value != null }
                .associate { (key, value) -> key to value!! }

        val options = options.copy(env = env)

        logger.debug("Instance {} runtime identifier is {}", instanceId, generatedName)

        val image =
            requireNotNull(
                blueprintPropertyResolver.resolve(
                    property = processedBlueprint.build.docker.image,
                    blueprint = processedBlueprint,
                    context = resolutionContext,
                ),
            ) { "Docker image cannot be null" }

        val createResult =
            tryGenerateRuntime(
                instanceId = instanceId,
                options = options,
                image = image,
                generatedName = generatedName,
                onInstall = processedBlueprint.hooks.onInstall,
            )

        logger.debug("Instance {} creation result: {}", instanceId, createResult)
        return handleInitialInstanceCreationResult(createResult, instanceId, blueprint.id)
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
                dockerImageService.pullImage(image).collect { status ->
                    val instanceStatus: InstanceStatus =
                        when (status) {
                            is DockerImagePullStatus.NotFound -> InstanceStatus.ImageNotFound
                            is DockerImagePullStatus.Started -> InstanceStatus.ImagePullNeeded
                            is DockerImagePullStatus.Failed -> InstanceStatus.ImagePullFailed
                            is DockerImagePullStatus.Completed -> InstanceStatus.ImagePullCompleted
                            is DockerImagePullStatus.Progress -> InstanceStatus.ImagePullInProgress
                        }

                    logger.debug(
                        "Updating instance {} with new status due to Docker image pull: {}",
                        instanceId,
                        instanceStatus.label,
                    )

                    if (status is DockerImagePullStatus.Progress) {
                        activityLogStore.append(
                            ResourceId(instanceId),
                            LogEntry.Activity(
                                ts = System.currentTimeMillis(),
                                activity = ActivityType.UPDATE,
                                step = "image-pull",
                                progress = status.pull.progressDetail?.current ?: 0,
                                msg = status.pull.statusText,
                                seqId = -1,
                                persistentId = FramePersistentIdGenerator.generate(System.currentTimeMillis(), status.pull.statusText),
                            ),
                        )
                    }

                    instanceRepository.update(instanceId) {
                        this.status = instanceStatus.label
                    }

                    if (instanceStatus == InstanceStatus.ImagePullCompleted) {
                        tryGenerateRuntime(instanceId, image, generatedName, options, onInstall)
                    }
                }
            }

            is GenerateRuntimeResult.NetworkConnectFailed, is GenerateRuntimeResult.RuntimeCreationFailed -> {
                logger.error("Unexpected instance {} runtime generation result: {}", instanceId, result)
            }
        }

        return result
    }

    private suspend fun handleInitialInstanceCreationResult(
        result: GenerateRuntimeResult,
        instanceId: Uuid,
        blueprintId: Uuid,
    ): Instance =
        when (result) {
            is GenerateRuntimeResult.Done -> {
                registerInstance(
                    instanceId = instanceId,
                    blueprintId = blueprintId,
                    status = InstanceStatus.Created,
                    containerId = result.runtimeIdentifier,
                    address = result.address,
                )
            }

            is GenerateRuntimeResult.RuntimeCreationFailed -> {
                registerInstance(
                    instanceId = instanceId,
                    blueprintId = blueprintId,
                    status = InstanceStatus.Unavailable,
                    containerId = null,
                    address = result.address,
                )
            }

            is GenerateRuntimeResult.NetworkConnectFailed -> {
                registerInstance(
                    instanceId = instanceId,
                    blueprintId = blueprintId,
                    status = InstanceStatus.NetworkAssignmentFailed,
                    containerId = result.runtimeIdentifier,
                    address = result.address,
                )
            }

            is GenerateRuntimeResult.MissingDockerImage -> {
                registerInstance(
                    instanceId = instanceId,
                    blueprintId = blueprintId,
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
                dockerContainerService.createContainer(
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
        val runtime = if (containerId == null) null else instanceRuntimeBuilder.buildRuntime(containerId)
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

    private fun generateContainerName(
        instanceId: Uuid,
        nameFormat: String?,
    ): String {
        val format = nameFormat ?: "kk-{node}-{id}"

        return format
            .replace("{id}", instanceId.toString())
            .replace("{node}", kukenConfig.node)
    }

    suspend fun getReachableInstance(instanceId: Uuid): InstanceEntity {
        val instance = instanceRepository.findById(instanceId) ?: throw InstanceNotFoundException()
        if (instance.containerId == null) {
            throw InstanceUnreachableRuntimeException()
        }

        return instance
    }

    override suspend fun deleteInstance(instanceId: Uuid) {
        val instance = instanceRepository.delete(instanceId) ?: throw InstanceNotFoundException()
        val containerId = instance.containerId ?: throw InstanceUnreachableRuntimeException()

        dockerContainerService.removeContainer(containerId, force = true)
    }

    override suspend fun startInstance(instanceId: Uuid) {
        dockerContainerService.startContainer(getInstanceContainerId(instanceId))
    }

    override suspend fun stopInstance(instanceId: Uuid) {
        dockerContainerService.stopContainer(getInstanceContainerId(instanceId))
    }

    override suspend fun runInstanceCommand(
        instanceId: Uuid,
        commandToRun: String,
    ): Int? {
        val instance = getInstance(instanceId)
        return instanceCommandExecutor.executeCommand(instance, commandToRun)
    }
}
