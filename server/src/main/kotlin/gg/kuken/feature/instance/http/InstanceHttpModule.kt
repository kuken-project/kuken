package gg.kuken.feature.instance.http

import gg.kuken.feature.instance.http.routes.deleteFile
import gg.kuken.feature.instance.http.routes.getInstanceDetails
import gg.kuken.feature.instance.http.routes.listFiles
import gg.kuken.feature.instance.http.routes.readFile
import gg.kuken.feature.instance.http.routes.writeFile
import gg.kuken.feature.instance.websocket.InstanceLogsRequestWebSocketClientMessageHandler
import gg.kuken.http.HttpModule
import gg.kuken.http.websocket.WebSocketClientMessageHandler
import gg.kuken.http.websocket.WebSocketOp
import gg.kuken.http.websocket.WebSocketOpCodes
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.routing
import org.koin.core.component.get

internal object InstanceHttpModule : HttpModule() {
    override fun install(app: Application): Unit =
        with(app) {
            routing {
                authenticate {
                    getInstanceDetails()
                    listFiles()
                    readFile()
                    writeFile()
                    deleteFile()
                }
            }
        }

    override fun webSocketHandlers(): Map<WebSocketOp, WebSocketClientMessageHandler> =
        mapOf(
            WebSocketOpCodes.InstanceLogsRequest to
                InstanceLogsRequestWebSocketClientMessageHandler(
                    instanceService = get(),
                    dockerClient = get(),
                ),
        )
}
