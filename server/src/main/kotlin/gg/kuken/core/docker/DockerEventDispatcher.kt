package gg.kuken.core.docker

import gg.kuken.core.EventDispatcher
import gg.kuken.core.EventDispatcherImpl
import gg.kuken.feature.instance.event.InstanceEvent
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
import me.devnatan.dockerkt.DockerClient
import me.devnatan.dockerkt.models.system.Event
import me.devnatan.dockerkt.models.system.EventAction
import me.devnatan.dockerkt.models.system.EventActor
import me.devnatan.dockerkt.models.system.EventType
import me.devnatan.dockerkt.resource.system.events
import kotlin.uuid.Uuid

class DockerEventDispatcher(
    dispatcher: EventDispatcherImpl = EventDispatcherImpl(),
    val dockerClient: DockerClient,
) : EventDispatcher by dispatcher {
    init {
        launch(
            context = CoroutineName("DockerEventDispatcher#consumer"),
        ) {
            dockerClient.system
                .events {
                    filterByType(EventType.Container)
                    filterByAction(EventAction.Start)
                }.collect(::interceptDockerEvent)
        }
    }

    private suspend fun interceptDockerEvent(event: Event) {
        when (event.action) {
            EventAction.Start -> dispatch(InstanceEvent.InstanceStartedEvent(event.actor.getInstanceId()))
            else -> Unit
        }
    }

    private fun EventActor.getInstanceId(): Uuid =
        requireNotNull(attributes["gg.kuken.instance.id"]?.let(Uuid.Companion::parse)) {
            "Instance id not found in event actor attributes: $attributes"
        }
}
