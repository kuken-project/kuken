package gg.kuken.feature.instance.http.routes

import gg.kuken.feature.instance.InstanceService
import gg.kuken.feature.instance.LogEntry
import gg.kuken.feature.instance.http.InstanceRoutes
import gg.kuken.feature.instance.http.dto.FetchLogsResponse
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.atomicfu.atomic
import me.devnatan.dockerkt.DockerClient
import me.devnatan.dockerkt.models.Stream
import me.devnatan.dockerkt.models.container.ContainerLogsResult
import me.devnatan.dockerkt.resource.container.logs
import org.koin.ktor.ext.inject

fun Route.fetchLogs() {
    val instanceService by inject<InstanceService>()
    val dockerClient by inject<DockerClient>()

    get<InstanceRoutes.FetchLogs> { parameters ->
        val container = instanceService.getInstanceContainerId(parameters.instanceId)

        val limit = parameters.limit
        val logs =
            dockerClient.containers.logs(container) {
                follow = false
                stdout = true
                stderr = true
                showTimestamps = true
                tail = limit?.toString()
                since = parameters.after?.div(1000)
                until = parameters.before?.div(1000)
            }
        check(logs is ContainerLogsResult.Complete)

        val seqCounter = atomic(0L)
        val splitted = logs.output.split("\n")
        val frames =
            splitted.mapNotNull { content ->
                if (content.isEmpty()) return@mapNotNull null

                LogEntry.Console
                    .fromText(content, Stream.StdOut)
                    .copy(seqId = seqCounter.incrementAndGet())
            }

        val hasMore =
            when {
                parameters.after != null || parameters.before != null -> true
                else -> limit != null && frames.size >= limit
            }

        call.respond(FetchLogsResponse(frames, hasMore = hasMore))
    }
}
