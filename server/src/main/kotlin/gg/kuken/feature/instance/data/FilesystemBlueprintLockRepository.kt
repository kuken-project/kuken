package gg.kuken.feature.instance.data

import gg.kuken.KukenConfig
import gg.kuken.feature.instance.data.repository.BlueprintLockRepository
import gg.kuken.feature.instance.model.BlueprintLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.uuid.Uuid

class FilesystemBlueprintLockRepository(
    private val kukenConfig: KukenConfig,
) : BlueprintLockRepository {
    private val json = Json { encodeDefaults = true }

    private fun lockPath(instanceId: Uuid) =
        kukenConfig.engine.instancesDataDirectory
            .resolve(instanceId.toString())
            .resolve(LOCK_FILENAME)

    override suspend fun findByInstanceId(instanceId: Uuid): BlueprintLock? =
        withContext(Dispatchers.IO) {
            val path = lockPath(instanceId)
            if (!path.exists()) return@withContext null
            json.decodeFromString(BlueprintLock.serializer(), path.readText())
        }

    override suspend fun save(instanceId: Uuid, lock: BlueprintLock) {
        withContext(Dispatchers.IO) {
            val path = lockPath(instanceId)
            path.parent.createDirectories()
            path.writeText(json.encodeToString(BlueprintLock.serializer(), lock))
        }
    }

    override suspend fun deleteByInstanceId(instanceId: Uuid) {
        withContext(Dispatchers.IO) {
            lockPath(instanceId).deleteIfExists()
        }
    }

    companion object {
        private const val LOCK_FILENAME = "lock.json"
    }
}
