package gg.kuken.feature.unit.repository

import gg.kuken.feature.unit.entity.UnitEntity
import gg.kuken.feature.unit.model.KukenUnit
import kotlin.uuid.Uuid

interface UnitRepository {
    suspend fun listUnits(): List<UnitEntity>

    suspend fun findById(id: Uuid): UnitEntity?

    suspend fun createUnit(unit: KukenUnit)
}
