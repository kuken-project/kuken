package gg.kuken.feature.unit.http.mapper

import gg.kuken.feature.instance.InstanceService
import gg.kuken.feature.unit.http.dto.UnitResponse
import gg.kuken.feature.unit.model.KukenUnit

class UnitMapper(
    val unitInstanceMapper: UnitInstanceMapper,
    val instanceService: InstanceService,
) {
    suspend operator fun invoke(unit: KukenUnit): UnitResponse {
        val instance = unit.instanceId?.let { instanceService.getInstance(it) }
        return UnitResponse(
            id = unit.id.toHexDashString(),
            externalId = unit.externalId,
            name = unit.name,
            createdAt = unit.createdAt,
            updatedAt = unit.updatedAt,
            deletedAt = unit.deletedAt,
            instance = instance?.let { unitInstanceMapper(it) },
            status = unit.status.value,
        )
    }
}
