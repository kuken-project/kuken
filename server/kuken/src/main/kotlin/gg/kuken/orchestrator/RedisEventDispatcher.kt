package gg.kuken.orchestrator

import gg.kuken.core.EventDispatcher
import io.lettuce.core.RedisClient
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

class RedisEventDispatcher(
    redisClient: RedisClient,
) : EventDispatcher {
    private val logger: Logger = LogManager.getLogger(RedisEventDispatcher::class.java)
    private val json: Json = Json { ignoreUnknownKeys = true }
    private val connection = redisClient.connectPubSub().reactive()

    override val coroutineContext: CoroutineContext get() =
        Dispatchers.IO +
            CoroutineName(RedisEventDispatcher::class.qualifiedName!!)

    override suspend fun dispatch(event: Any) {
        logger.info("Event sent: ${event::class.jvmName}")

        val json =
            json.encodeToString(
                serializer = serializer(event::class.java),
                value = event,
            )
        connection.publish(event::class.qualifiedName, json).awaitSingle()
    }

    override suspend fun <T : Any> listen(eventType: KClass<T>): Flow<T> =
        callbackFlow {
            connection.subscribe(eventType.qualifiedName)

            val codec = serializer(eventType.java)
            val observer =
                connection.observeChannels().subscribe { message ->
                    logger.info("Event received: ${eventType.jvmName}")

                    @Suppress("UNCHECKED_CAST")
                    val json =
                        json.decodeFromString(
                            deserializer = codec,
                            string = message.message,
                        ) as T

                    trySend(json)
                }

            awaitClose {
                observer.dispose()
            }
        }
}
