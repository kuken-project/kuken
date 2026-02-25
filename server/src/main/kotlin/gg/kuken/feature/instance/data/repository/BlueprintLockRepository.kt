package gg.kuken.feature.instance.data.repository

import gg.kuken.feature.instance.model.BlueprintLock
import kotlin.uuid.Uuid

interface BlueprintLockRepository {
    suspend fun findByInstanceId(instanceId: Uuid): BlueprintLock?

    suspend fun save(instanceId: Uuid, lock: BlueprintLock)

    suspend fun deleteByInstanceId(instanceId: Uuid)
}
