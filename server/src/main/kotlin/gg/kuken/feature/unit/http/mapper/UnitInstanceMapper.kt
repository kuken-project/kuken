package gg.kuken.feature.unit.http.mapper

import gg.kuken.feature.blueprint.http.dto.BlueprintResponse
import gg.kuken.feature.blueprint.service.BlueprintService
import gg.kuken.feature.instance.model.Instance
import gg.kuken.feature.unit.http.dto.UnitResponse

class UnitInstanceMapper(
    val blueprintService: BlueprintService,
) {
    suspend operator fun invoke(instance: Instance): UnitResponse.Instance {
        val blueprint = blueprintService.getBlueprint(instance.blueprintId)

        return UnitResponse.Instance(
            id = instance.id.toHexDashString(),
            address = instance.address,
            status = instance.status.label,
            nodeId = instance.nodeId,
            created = instance.createdAt,
            blueprint = BlueprintResponse(blueprint),
        )
    }
}
