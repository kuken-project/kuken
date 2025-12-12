package gg.kuken.feature.unit.model

import kotlinx.serialization.Serializable

@Serializable
enum class UnitStatus(
    val value: String,
) {
    Unknown("unknown"),
    Created("created"),
    MissingInstance("missing-instance"),
    CreatingInstance("creating-instance"),
    Ready("ready"),
    ;

    companion object {
        fun getByValue(value: String): UnitStatus =
            entries.firstOrNull {
                it.value.equals(value, ignoreCase = false)
            } ?: Unknown
    }
}
