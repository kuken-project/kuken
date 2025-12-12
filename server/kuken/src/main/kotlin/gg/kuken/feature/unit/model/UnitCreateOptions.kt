package gg.kuken.feature.unit.model

import kotlin.uuid.Uuid

data class UnitCreateOptions(
    val name: String,
    val blueprintId: Uuid,
    val image: String,
    val options: Map<String, String>,
    val network: Network?,
    val externalId: String?,
    val actorId: Uuid?,
) {
    data class Network(
        val host: String?,
        val port: UShort?,
    )
}
