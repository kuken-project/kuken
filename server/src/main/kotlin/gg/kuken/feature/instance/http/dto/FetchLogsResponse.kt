package gg.kuken.feature.instance.http.dto

import gg.kuken.feature.instance.model.ConsoleLogFrame
import kotlinx.serialization.Serializable

@Serializable
data class FetchLogsResponse(
    val frames: List<ConsoleLogFrame>,
    val hasMore: Boolean,
)
