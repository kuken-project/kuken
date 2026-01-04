package gg.kuken.feature.instance.http.routes

import gg.kuken.feature.instance.InstanceFileService
import gg.kuken.feature.instance.http.InstanceRoutes
import io.ktor.server.request.receiveText
import io.ktor.server.routing.Route
import io.ktor.server.routing.put
import org.koin.ktor.ext.inject

fun Route.writeFile() {
    val instanceFileService by inject<InstanceFileService>()

    put<InstanceRoutes.FileContents> { parameters ->
        val contents = call.receiveText()
        instanceFileService.writeFile(
            instanceId = parameters.instanceId,
            filePath = parameters.path,
            contents = contents,
        )
    }
}
