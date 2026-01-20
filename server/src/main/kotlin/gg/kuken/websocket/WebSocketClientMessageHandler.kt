package gg.kuken.websocket

abstract class WebSocketClientMessageHandler {
    abstract suspend fun WebSocketClientMessageContext.handle()
}
