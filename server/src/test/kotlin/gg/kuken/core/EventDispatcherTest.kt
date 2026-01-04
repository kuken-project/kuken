package gg.kuken.core

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private data class TestEvent<T>(
    val value: T,
)

private sealed class SealedEvent {
    data class TestEvent(
        val value: String,
    ) : SealedEvent()

    data class AnotherEvent(
        val value: String,
    ) : SealedEvent()
}

@ExperimentalCoroutinesApi
class EventDispatcherTest {
    @Test
    fun `listen to a publication of a primitive type`() =
        runTest {
            val eventsDispatcher: EventDispatcher = EventDispatcherImpl()
            val received = mutableListOf<Int>()

            eventsDispatcher
                .listen<Int>()
                .onEach(received::add)
                .launchIn(TestScope(UnconfinedTestDispatcher()))

            assertTrue(received.isEmpty())
            eventsDispatcher.dispatch(event = 3)

            assertEquals(expected = listOf(3), actual = received)
        }

    @Test
    fun `listen to a publication of a data class`() =
        runTest {
            val eventsDispatcher: EventDispatcher = EventDispatcherImpl()
            val received = mutableListOf<TestEvent<String>>()

            eventsDispatcher
                .listen<TestEvent<String>>()
                .onEach(received::add)
                .launchIn(TestScope(UnconfinedTestDispatcher()))

            assertTrue(received.isEmpty())
            eventsDispatcher.dispatch(event = TestEvent("abc"))

            assertEquals(listOf(element = TestEvent("abc")), received)
        }

    @Test
    fun `listen to a publication of a sealed type`() =
        runTest {
            val eventsDispatcher: EventDispatcher = EventDispatcherImpl()
            val received = mutableListOf<SealedEvent>()

            eventsDispatcher
                .listen<SealedEvent>()
                .onEach(received::add)
                .launchIn(TestScope(UnconfinedTestDispatcher()))

            assertTrue(received.isEmpty())
            eventsDispatcher.dispatch(event = SealedEvent.TestEvent("abc"))
            eventsDispatcher.dispatch(event = SealedEvent.AnotherEvent("def"))

            assertEquals(
                actual = received,
                expected = listOf(SealedEvent.TestEvent("abc"), SealedEvent.AnotherEvent("def")),
            )
        }

    @Test
    fun `ignore publication of not listened type`() =
        runTest {
            val eventsDispatcher: EventDispatcher = EventDispatcherImpl()
            val received = mutableListOf<String>()

            eventsDispatcher
                .listen<String>()
                .onEach(received::add)
                .launchIn(TestScope(UnconfinedTestDispatcher()))

            assertTrue(received.isEmpty())
            eventsDispatcher.dispatch(event = TestEvent("abc"))

            assertTrue(received.isEmpty())
        }
}
