package gg.kuken.feature.instance.data.repository

import gg.kuken.feature.instance.data.entity.InstanceEntity
import gg.kuken.feature.instance.model.Instance
import kotlin.uuid.Uuid

interface InstanceRepository {
    suspend fun findById(id: Uuid): InstanceEntity?

    suspend fun findContainerById(instanceId: Uuid): String?

    suspend fun create(instance: Instance)

    suspend fun delete(id: Uuid): InstanceEntity?

    suspend fun update(
        id: Uuid,
        update: InstanceEntity.() -> Unit,
    ): InstanceEntity?

    suspend fun markOutdatedByBlueprintId(blueprintId: Uuid)
}
