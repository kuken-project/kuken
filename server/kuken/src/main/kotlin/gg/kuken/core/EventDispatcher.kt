package gg.kuken.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.merge
import kotlin.reflect.KClass

interface EventDispatcher : CoroutineScope {

    fun dispatch(event: Any)

    fun <T : Any> listen(eventType: KClass<T>): Flow<T>
}

inline fun <reified T : Any> EventDispatcher.listen(): Flow<T> {
    return listen(T::class)
}

internal class EventDispatcherImpl :
    EventDispatcher,
    CoroutineScope by CoroutineScope(Dispatchers.IO + SupervisorJob()) {

    private val publisher = MutableSharedFlow<Any>(extraBufferCapacity = 1)

    override fun <T : Any> listen(eventType: KClass<T>): Flow<T> =
        publisher.filterIsInstance(eventType)

    override fun dispatch(event: Any) {
        publisher.tryEmit(event)
    }
}

internal class CompositeEventDispatcher(
    private val dispatchers: List<EventDispatcher>
) : EventDispatcher {

    override val coroutineContext = SupervisorJob() + Dispatchers.IO

    override fun dispatch(event: Any) {
        dispatchers.forEach { it.dispatch(event) }
    }

    override fun <T : Any> listen(eventType: KClass<T>): Flow<T> =
        dispatchers.map { it.listen(eventType) }
            .merge()
}