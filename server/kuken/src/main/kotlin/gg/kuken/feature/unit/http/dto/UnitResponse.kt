package gg.kuken.feature.unit.http.dto

import gg.kuken.feature.unit.model.KukenUnit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
internal data class UnitResponse(
    @SerialName("id") val id: Uuid,
    @SerialName("external") val externalId: String?,
    @SerialName("node") val nodeId: String,
    @SerialName("name") val name: String,
    @SerialName("created") val createdAt: Instant,
    @SerialName("updated") val updatedAt: Instant,
    @SerialName("deleted") val deletedAt: Instant?,
    @SerialName("instance") val instanceId: String?,
    @SerialName("status") val status: String,
) {
    constructor(value: KukenUnit) : this(
        id = value.id,
        externalId = value.externalId,
        nodeId = value.nodeId,
        name = value.name,
        createdAt = value.createdAt,
        updatedAt = value.updatedAt,
        deletedAt = value.deletedAt,
        instanceId = value.instanceId?.toString(),
        status = value.status.value,
    )
}
