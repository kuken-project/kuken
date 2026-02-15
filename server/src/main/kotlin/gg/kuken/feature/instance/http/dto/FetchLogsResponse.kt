package gg.kuken.feature.instance.http.dto

import gg.kuken.feature.instance.LogEntry
import kotlinx.serialization.Serializable

@Serializable
data class FetchLogsResponse(
    val frames: List<LogEntry>,
    val hasMore: Boolean,
)
