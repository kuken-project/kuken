package gg.kuken.feature.instance.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
class Instance(
    val id: Uuid,
    val status: InstanceStatus,
    val containerId: String?,
    val updatePolicy: ImageUpdatePolicy,
    val address: HostPort?,
    val blueprintId: Uuid,
    val createdAt: Instant,
    val nodeId: String,
    val blueprintOutdated: Boolean = false,
)
