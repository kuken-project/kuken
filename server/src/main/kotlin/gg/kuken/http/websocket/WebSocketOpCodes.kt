package gg.kuken.http.websocket

typealias WebSocketOp = Int

@Suppress("ConstPropertyName")
object WebSocketOpCodes {
    const val InstanceUnavailable: WebSocketOp = 1

    const val InstanceLogsRequest: WebSocketOp = 2

    const val InstanceLogsRequestStarted: WebSocketOp = 3

    const val InstanceLogsRequestFrame: WebSocketOp = 4

    const val InstanceLogsRequestFinished: WebSocketOp = 5
}
