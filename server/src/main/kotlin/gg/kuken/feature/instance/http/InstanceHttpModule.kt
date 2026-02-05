package gg.kuken.feature.instance.http

import gg.kuken.feature.instance.InstanceService
import gg.kuken.feature.instance.http.routes.command
import gg.kuken.feature.instance.http.routes.deleteFile
import gg.kuken.feature.instance.http.routes.fetchLogs
import gg.kuken.feature.instance.http.routes.getFileMetadata
import gg.kuken.feature.instance.http.routes.getInstanceDetails
import gg.kuken.feature.instance.http.routes.listFiles
import gg.kuken.feature.instance.http.routes.readFile
import gg.kuken.feature.instance.http.routes.renameFile
import gg.kuken.feature.instance.http.routes.uploadFiles
import gg.kuken.feature.instance.http.routes.writeFile
import gg.kuken.feature.instance.websocket.handlers.InstanceLogsPacketWSHandler
import gg.kuken.feature.instance.websocket.handlers.InstanceLogsRequestWSHandler
import gg.kuken.http.HttpModule
import gg.kuken.websocket.WebSocketClientMessage
import gg.kuken.websocket.WebSocketClientMessageContext
import gg.kuken.websocket.WebSocketClientMessageHandler
import gg.kuken.websocket.WebSocketOp
import gg.kuken.websocket.WebSocketOpCodes
import gg.kuken.websocket.uuid
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
                    renameFile()
                    uploadFiles()
                    getFileMetadata()
                    command()
                    fetchLogs()
                }
            }
        }

    override fun webSocketHandlers(): Map<WebSocketOp, WebSocketClientMessageHandler> =
        mapOf(
            WebSocketOpCodes.InstanceLogsRequest to
                InstanceLogsRequestWSHandler(
                    instanceService = get(),
                    dockerClient = get(),
                ),
            WebSocketOpCodes.InstanceLogsPacket to
                InstanceLogsPacketWSHandler(
                    instanceService = get(),
                    dockerClient = get(),
                ),
        )
}

context(_: WebSocketClientMessageContext, instanceService: InstanceService)
suspend fun WebSocketClientMessage.getInstance() = instanceService.getInstance(uuid("iid"))
