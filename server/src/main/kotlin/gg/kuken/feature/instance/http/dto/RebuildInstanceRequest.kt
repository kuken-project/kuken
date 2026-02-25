package gg.kuken.feature.instance.http.dto

import gg.kuken.feature.blueprint.processor.BlueprintResolutionContextEnv
import gg.kuken.feature.blueprint.processor.BlueprintResolutionContextInputs
import kotlinx.serialization.Serializable

@Serializable
internal data class RebuildInstanceRequest(
    val inputs: BlueprintResolutionContextInputs,
    val env: BlueprintResolutionContextEnv = emptyMap(),
)
