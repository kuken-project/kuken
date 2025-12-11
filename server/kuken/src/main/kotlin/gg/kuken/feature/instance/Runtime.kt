package gg.kuken.feature.instance

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class InstanceRuntime(
    val id: String,
    val network: InstanceRuntimeNetwork,
    val platform: String?,
    val exitCode: Int,
    val outOfMemory: Boolean,
    val error: String?,
    val status: String,
    val pid: Int,
    val fsPath: String?,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val mounts: List<InstanceRuntimeMount>,
)
