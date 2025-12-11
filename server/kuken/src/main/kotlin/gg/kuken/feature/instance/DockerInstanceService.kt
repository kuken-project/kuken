package gg.kuken.feature.instance

import gg.kuken.feature.instance.entity.InstanceEntity
import gg.kuken.feature.instance.model.HostPort
import gg.kuken.feature.instance.model.ImageUpdatePolicy
import gg.kuken.feature.instance.model.Instance
import gg.kuken.feature.instance.model.InstanceStatus
import gg.kuken.feature.instance.repository.InstanceRepository
import me.devnatan.dockerkt.DockerClient
import me.devnatan.dockerkt.resource.container.remove
import me.devnatan.dockerkt.resource.exec.create
import me.devnatan.dockerkt.resource.exec.start
import kotlin.jvm.Throws
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

class DockerInstanceService(
    private val dockerClient: DockerClient,
    private val instanceRepository: InstanceRepository,
) : InstanceService {
    override suspend fun getInstance(instanceId: Uuid): Instance {
        val instance = instanceRepository.findById(instanceId) ?: throw InstanceNotFoundException()

        return Instance(
            id = instance.id.value.toKotlinUuid(),
            status = instance.status.let(InstanceStatus::valueOf),
            containerId = instance.containerId,
            updatePolicy = instance.updatePolicy.let(ImageUpdatePolicy::getById),
            connection = HostPort(instance.host!!, instance.port!!),
            runtime = null,
            blueprintId = instance.blueprintId,
            createdAt = instance.createdAt,
            nodeId = instance.nodeId,
        )
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
