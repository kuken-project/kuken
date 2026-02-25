package gg.kuken.feature.remoteConfig

import gg.kuken.core.database.dbQuery

class RemoteConfigService(
    private val remoteConfigRepository: RemoteConfigRepository,
) {
    suspend fun getConfigValue(key: RemoteConfigKey): String =
        remoteConfigRepository.findConfigValue(key.name) ?: error("Missing config: $key")

    suspend fun isConfigValueSet(key: RemoteConfigKey): Boolean = remoteConfigRepository.existsConfigValue(key.name)

    suspend fun setConfigValue(
        key: RemoteConfigKey,
        value: String,
    ) = dbQuery {
        remoteConfigRepository.updateConfigValue(key.name, value)
    }
}
