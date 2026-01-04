package gg.kuken.feature.unit.http.dto

import gg.kuken.feature.instance.model.HostPort
import gg.kuken.feature.instance.model.InstanceStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class UnitResponse(
    @SerialName("id") val id: String,
    @SerialName("external") val externalId: String?,
    @SerialName("node") val nodeId: String,
    @SerialName("name") val name: String,
    @SerialName("created") val createdAt: Instant,
    @SerialName("updated") val updatedAt: Instant,
    @SerialName("deleted") val deletedAt: Instant?,
    @SerialName("status") val status: String,
    @SerialName("instance") val instance: Instance?,
) {
    @Serializable
    data class Instance(
        val id: String,
        val address: HostPort?,
        val status: String,
        val nodeId: String,
        val created: Instant,
        val blueprint: Blueprint,
    ) {
        @Serializable
        data class Blueprint(
            val id: String,
            val iconUrl: String?,
        )
    }
}
