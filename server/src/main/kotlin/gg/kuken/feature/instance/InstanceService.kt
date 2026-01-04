package gg.kuken.feature.instance

import gg.kuken.feature.instance.model.CreateInstanceOptions
import gg.kuken.feature.instance.model.Instance
import kotlin.uuid.Uuid

interface InstanceService {
    suspend fun getInstance(instanceId: Uuid): Instance

    suspend fun getInstanceNoRuntime(instanceId: Uuid): Instance

    suspend fun getInstanceContainerId(instanceId: Uuid): String

    suspend fun createInstance(
        blueprintId: Uuid,
        options: CreateInstanceOptions,
    ): Instance

    suspend fun runInstanceCommand(
        instanceId: Uuid,
        commandToRun: String,
    )

    suspend fun startInstance(instanceId: Uuid)

    suspend fun stopInstance(instanceId: Uuid)

    suspend fun deleteInstance(instanceId: Uuid)
}
