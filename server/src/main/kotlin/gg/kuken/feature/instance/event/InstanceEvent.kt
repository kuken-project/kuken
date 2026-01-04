package gg.kuken.feature.instance.event

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface InstanceEvent {
    @Serializable
    data class InstanceStartedEvent(
        val instanceId: Uuid,
    ) : InstanceEvent
}
