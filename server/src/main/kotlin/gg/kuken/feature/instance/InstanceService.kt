package gg.kuken.feature.instance

import gg.kuken.feature.blueprint.processor.BlueprintResolutionContextEnv
import gg.kuken.feature.blueprint.processor.BlueprintResolutionContextInputs
import gg.kuken.feature.instance.model.CreateInstanceOptions
import gg.kuken.feature.instance.model.Instance
import kotlin.uuid.Uuid

interface InstanceService {
    suspend fun getInstance(instanceId: Uuid): Instance

    suspend fun getInstanceWithRuntime(instanceId: Uuid): Instance

    suspend fun getInstanceContainerId(instanceId: Uuid): String

    suspend fun createInstance(options: CreateInstanceOptions): Instance

    suspend fun runInstanceCommand(
        instanceId: Uuid,
        commandToRun: String,
    ): Int?

    suspend fun startInstance(instanceId: Uuid)

    suspend fun stopInstance(instanceId: Uuid)

    suspend fun rebuildInstance(
        instanceId: Uuid,
        inputs: BlueprintResolutionContextInputs,
        env: BlueprintResolutionContextEnv,
    ): Instance

    suspend fun deleteInstance(instanceId: Uuid)
}
