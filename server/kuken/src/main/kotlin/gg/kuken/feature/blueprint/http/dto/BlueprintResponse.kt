package gg.kuken.feature.blueprint.http.dto

import gg.kuken.feature.blueprint.model.Blueprint
import gg.kuken.feature.blueprint.model.BlueprintSpec
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
internal data class BlueprintResponse(
    val id: Uuid,
    @SerialName("created-at") val createdAt: Instant,
    @SerialName("updated-at") val updatedAt: Instant,
    @SerialName("spec") val spec: BlueprintSpec,
) {
    constructor(blueprint: Blueprint) : this(
        id = blueprint.id,
        createdAt = blueprint.createdAt,
        updatedAt = blueprint.updatedAt,
        spec = blueprint.spec,
    )
}
