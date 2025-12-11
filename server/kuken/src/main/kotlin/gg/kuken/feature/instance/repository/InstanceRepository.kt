@file:OptIn(ExperimentalUuidApi::class)

package gg.kuken.feature.instance.repository

import gg.kuken.feature.instance.entity.InstanceEntity
import gg.kuken.feature.instance.model.Instance
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface InstanceRepository {
    suspend fun findById(id: Uuid): InstanceEntity?

    suspend fun create(instance: Instance)

    suspend fun delete(id: Uuid)

    suspend fun update(
        id: Uuid,
        update: InstanceEntity.() -> Unit,
    ): InstanceEntity?
}
