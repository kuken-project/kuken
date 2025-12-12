package gg.kuken.feature.unit.http

import io.ktor.resources.Resource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
@Resource("/units")
internal class UnitRoutes {
    @Serializable
    @Resource("")
    internal class All(
        @Suppress("UNUSED") val parent: UnitRoutes = UnitRoutes(),
    )

    @Serializable
    @Resource("{unit}")
    internal class ById(
        @Suppress("UNUSED") val parent: UnitRoutes = UnitRoutes(),
        @SerialName("unit")
        val unitId: Uuid,
    )
}
