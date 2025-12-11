package gg.kuken.feature.blueprint

import gg.kuken.feature.blueprint.repository.BlueprintRepository
import kotlinx.serialization.json.Json

class BlueprintService(
    val blueprintRepository: BlueprintRepository,
    val blueprintSpecProvider: BlueprintSpecProvider,
) {
    private val json: Json =
        Json {
            coerceInputValues = false
            prettyPrint = true
        }
}
