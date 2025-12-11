package gg.kuken.feature.blueprint.repository

import gg.kuken.feature.blueprint.entity.BlueprintEntity
import kotlinx.datetime.Instant
import kotlin.uuid.Uuid

interface BlueprintRepository {
    suspend fun findAll(): List<BlueprintEntity>

    suspend fun find(id: Uuid): BlueprintEntity?

    suspend fun create(
        id: Uuid,
        spec: ByteArray,
        createdAt: Instant,
    )

    suspend fun delete(id: Uuid)
}
