package gg.kuken.feature.instance.http

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable
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

    @Serializable
    @Resource("{instanceId}/files")
    class File(
        val parent: InstanceRoutes = InstanceRoutes(),
        val instanceId: Uuid,
        val path: String,
    )

    @Serializable
    @Resource("{instanceId}/files/contents")
    class FileContents(
        val parent: InstanceRoutes = InstanceRoutes(),
        val instanceId: Uuid,
        val path: String,
    )

    @Serializable
    @Resource("{instanceId}/files/list")
    class ListFiles(
        val parent: InstanceRoutes = InstanceRoutes(),
        val instanceId: Uuid,
        val path: String,
    )

    @Serializable
    @Resource("{instanceId}/command")
    class Command(
        val parent: InstanceRoutes = InstanceRoutes(),
        val instanceId: Uuid,
    )

    @Serializable
    @Resource("{instanceId}/logs")
    class FetchLogs(
        val parent: InstanceRoutes = InstanceRoutes(),
        val instanceId: Uuid,
        val limit: Int? = null,
        val before: Long? = null,
        val after: Long? = null,
    )

    @Serializable
    @Resource("{instanceId}/rebuild")
    class Rebuild(
        val parent: InstanceRoutes = InstanceRoutes(),
        val instanceId: Uuid,
    )
}
