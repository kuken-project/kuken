package gg.kuken.feature.instance.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class BlueprintLock(
    val blueprintId: Uuid,
    val blueprintVersion: String,
    val metadata: LockMetadata,
    val docker: LockDockerConfig,
    val environmentVariables: Map<String, String>,
    val inputs: Map<String, String>,
    val instanceSettings: LockInstanceSettings?,
    val resources: List<LockResource>,
    val hooks: LockHooks,
)

@Serializable
data class LockMetadata(
    val name: String,
    val version: String,
    val url: String,
    val author: String,
    val iconPath: String,
)

@Serializable
data class LockDockerConfig(
    val image: String,
)

@Serializable
data class LockInstanceSettings(
    val startup: String?,
    val commandExecutor: LockCommandExecutor?,
)

@Serializable
sealed class LockCommandExecutor {
    @Serializable
    @SerialName("rcon")
    data class Rcon(
        val port: Int,
        val password: String,
        val template: String,
    ) : LockCommandExecutor()

    @Serializable
    @SerialName("ssh")
    data class SSH(
        val template: String,
    ) : LockCommandExecutor()
}

@Serializable
data class LockResource(
    val name: String,
    val source: String,
)

@Serializable
data class LockHooks(
    val onInstall: LockResource?,
)
