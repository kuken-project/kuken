package gg.kuken.feature.instance

import gg.kuken.core.EventDispatcher
import gg.kuken.core.listen
import gg.kuken.feature.instance.event.InstanceEvent
import gg.kuken.websocket.WebSocketManager
import gg.kuken.websocket.WebSocketOpCodes
import gg.kuken.websocket.WebSocketServerMessage
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class InstanceEventsRedirector(
    val webSocketManager: WebSocketManager,
    val eventDispatcher: EventDispatcher,
) : CoroutineScope {
    private val json: Json = Json { ignoreUnknownKeys = true }

    override val coroutineContext = webSocketManager.coroutineContext

    init {
        launch(Dispatchers.Default + CoroutineName("InstanceEventRedirector")) {
            eventDispatcher.listen<InstanceEvent>().collect { event ->
                if (!webSocketManager.isReceivingEvents()) {
                    return@collect
                }

                val message = translate(event)
                val payload =
                    io.ktor.websocket.Frame
                        .Text(json.encodeToString(message))

                webSocketManager.broadcasting { session ->
                    session.connection.outgoing.send(payload)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun translate(event: InstanceEvent): WebSocketServerMessage<*> =
        when (event) {
            is InstanceEvent.InstanceStartedEvent -> {
                WebSocketServerMessage(
                    op = WebSocketOpCodes.InstanceStarted,
                    data = event.instanceId,
                )
            }

            is InstanceEvent.InstanceStoppedEvent -> {
                WebSocketServerMessage(
                    op = WebSocketOpCodes.InstanceStopped,
                    data = event.instanceId,
                )
            }
        } as WebSocketServerMessage<*>
}
