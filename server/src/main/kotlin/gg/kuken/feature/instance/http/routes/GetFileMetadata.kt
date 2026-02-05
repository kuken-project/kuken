package gg.kuken.feature.instance.http.routes

import gg.kuken.feature.instance.InstanceFileService
import gg.kuken.feature.instance.http.InstanceRoutes
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject

fun Route.getFileMetadata() {
    val instanceFileService by inject<InstanceFileService>()

    get<InstanceRoutes.File> { parameters ->
        val file = instanceFileService.getFile(parameters.instanceId, parameters.path)
        call.respond(file)
    }
}
