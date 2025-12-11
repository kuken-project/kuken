package gg.kuken.feature.blueprint.http

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
@Resource("/blueprints")
class BlueprintRoutes {
    @Serializable
    @Resource("")
    class All(
        val parent: BlueprintRoutes = BlueprintRoutes(),
    )

    @Serializable
    @Resource("{blueprintId}")
    class ById(
        val parent: BlueprintRoutes = BlueprintRoutes(),
        val blueprintId: Uuid,
    )

    @Serializable
    @Resource("import")
    class Import(
        val parent: BlueprintRoutes = BlueprintRoutes(),
    )
}
