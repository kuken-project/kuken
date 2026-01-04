package gg.kuken.feature.instance.http.routes

import gg.kuken.feature.instance.InstanceFileService
import gg.kuken.feature.instance.http.InstanceRoutes
import io.ktor.server.resources.delete
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject

fun Route.deleteFile() {
    val instanceFileService by inject<InstanceFileService>()

    delete<InstanceRoutes.FileContents> { parameters ->
        val contents =
            instanceFileService.deleteFile(
                instanceId = parameters.instanceId,
                filePath = parameters.path,
            )

        call.respond(contents)
    }
}
