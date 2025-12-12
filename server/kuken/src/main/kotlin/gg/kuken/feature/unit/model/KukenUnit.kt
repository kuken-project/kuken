package gg.kuken.feature.unit.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
class KukenUnit(
    val id: Uuid,
    val externalId: String?,
    val nodeId: String,
    val name: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,
    val instanceId: Uuid?,
    val status: UnitStatus,
)
