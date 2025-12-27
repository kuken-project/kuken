package gg.kuken.http.websocket

typealias WebSocketOp = Int

@Suppress("ConstPropertyName")
object WebSocketOpCodes {
    const val InstanceUnavailable: WebSocketOp = 1

    const val InstanceLogsRequest: WebSocketOp = 2
}
