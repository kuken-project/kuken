package gg.kuken.websocket

import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import kotlin.coroutines.CoroutineContext

abstract class WebSocketClientMessageHandler :
    CoroutineScope,
    KoinComponent {
    override lateinit var coroutineContext: CoroutineContext

    abstract suspend fun WebSocketClientMessageContext.handle()
}
