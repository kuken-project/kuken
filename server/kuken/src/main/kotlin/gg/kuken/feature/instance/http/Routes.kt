

package gg.kuken.feature.instance.http

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@Resource("/instances")
class InstanceRoutes {
    @Serializable
    @Resource("{instanceId}")
    class ById(
        val parent: InstanceRoutes = InstanceRoutes(),
        val instanceId: Uuid,
    )
}
