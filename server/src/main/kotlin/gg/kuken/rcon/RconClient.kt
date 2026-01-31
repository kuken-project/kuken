package gg.kuken.rcon

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readFully
import io.ktor.utils.io.writeFully
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import kotlin.system.measureTimeMillis
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class RconClient(
    private val config: RconServerConfig,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val logger = LoggerFactory.getLogger(RconClient::class.java)
    private val selectorManager = SelectorManager(dispatcher)
    private var socket: Socket? = null
    private var readChannel: ByteReadChannel? = null
    private var writeChannel: ByteWriteChannel? = null

    private val packetIdCounter = atomic(0)
    private val connected = atomic(false)
    private val authenticated = atomic(false)
    private val mutex = Mutex()

    private var connectionJob: Job? = null

    suspend fun connect(): Unit =
        withContext(dispatcher) {
            mutex.withLock {
                try {
                    if (connected.value) {
                        logger.info("Already connected to ${config.name}")
                        return@withContext
                    }

                    logger.info("Connecting to ${config.name} at ${config.host}:${config.port}...")

                    socket =
                        withTimeout(config.timeoutMs) {
                            aSocket(selectorManager).tcp().connect(InetSocketAddress(config.host, config.port))
                        }

                    readChannel = socket!!.openReadChannel()
                    writeChannel = socket!!.openWriteChannel(autoFlush = true)

                    connected.value = true
                    logger.info("Connected to ${config.name}, authenticating...")

                    authenticate()
                    logger.info("Successfully authenticated with ${config.name}")
                } catch (e: Throwable) {
                    logger.error("Failed to connect to ${config.name}", e)
                    disconnect()
                }
            }
        }

    private suspend fun authenticate() {
        val authPacket =
            RconPacket(
                id = nextPacketId(),
                type = RCON_SERVERDATA_AUTH,
                body = config.password,
            )

        sendPacket(authPacket)

        val responses = receivePackets(2)

        val authResponse = responses.lastOrNull { it.type == RCON_SERVERDATA_AUTH_RESPONSE }

        if (authResponse != null && authResponse.id != -1) {
            authenticated.value = true
            return
        }

        authenticated.value = false
        throw RconAuthException("Authentication failed: invalid password or server rejected")
    }

    suspend fun executeCommand(command: String): RconCommandResult {
        if (!connected.value || !authenticated.value) {
            connect()
        }

        return mutex.withLock {
            try {
                var response: String
                val executionTime =
                    measureTimeMillis {
                        val packetId = nextPacketId()
                        val commandPacket =
                            RconPacket(
                                id = packetId,
                                type = RCON_SERVERDATA_EXECCOMMAND,
                                body = command,
                            )

                        logger.debug("Executing command on ${config.name}: $command")
                        sendPacket(commandPacket)

                        response = receiveCommandResponse(packetId)
                    }

                RconCommandResult(
                    success = true,
                    response = response.trim(),
                    executionTimeMs = executionTime,
                    serverName = config.name,
                )
            } catch (e: Throwable) {
                logger.error("Error executing command on ${config.name}: $command", e)
                connected.value = false
                authenticated.value = false

                RconCommandResult(
                    success = false,
                    response = "",
                    executionTimeMs = 0,
                    serverName = config.name,
                    error = e.message ?: "Unknown error",
                )
            }
        }
    }

    private suspend fun sendPacket(packet: RconPacket) {
        val data = RconPacket.Codec.encode(packet)
        writeChannel?.writeFully(data)
    }

    private suspend fun receivePackets(expectedCount: Int): List<RconPacket> {
        val packets = mutableListOf<RconPacket>()

        repeat(expectedCount) {
            try {
                val packet =
                    withTimeout(config.timeoutMs) {
                        receivePacket()
                    }
                packet?.let { packets.add(it) }
            } catch (_: TimeoutCancellationException) {
                logger.debug("Timeout waiting for packet, continuing...")
            }
        }

        return packets
    }

    private suspend fun receiveCommandResponse(requestId: Int): String {
        val responseBuilder = StringBuilder()

        val terminatorId = nextPacketId()
        val terminatorPacket =
            RconPacket(
                id = terminatorId,
                type = RCON_SERVERDATA_RESPONSE_VALUE,
                body = "",
            )
        sendPacket(terminatorPacket)

        while (true) {
            val packet =
                withTimeout(config.timeoutMs) {
                    receivePacket()
                } ?: break

            if (packet.id == terminatorId) {
                break
            }

            if (packet.id == requestId || packet.type == RCON_SERVERDATA_RESPONSE_VALUE) {
                responseBuilder.append(packet.body)
            }
        }

        return responseBuilder.toString()
    }

    private suspend fun receivePacket(): RconPacket? {
        val channel = readChannel ?: return null

        val sizeBuffer = ByteArray(4)
        channel.readFully(sizeBuffer)

        val size = ByteBuffer.wrap(sizeBuffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt()

        if (size < RCON_PACKET_HEADER_SIZE || size > RCON_PACKET_MAX_BODY_SIZE + RCON_PACKET_HEADER_SIZE) {
            logger.warn("Invalid packet size: $size")
            return null
        }

        val packetData = ByteArray(size)
        channel.readFully(packetData)

        val fullPacket = sizeBuffer + packetData
        return RconPacket.Codec.decode(fullPacket)
    }

    suspend fun ping(): Long {
        val startTime = System.currentTimeMillis()
        val result = executeCommand("")
        if (!result.success) {
            throw RuntimeException(result.error)
        }

        return System.currentTimeMillis() - startTime
    }

    suspend fun disconnect() {
        mutex.withLock {
            try {
                connectionJob?.cancel()
                socket?.close()
            } finally {
                socket = null
                readChannel = null
                writeChannel = null
                connected.value = false
                authenticated.value = false
            }
        }
    }

    private fun nextPacketId(): Int = packetIdCounter.incrementAndGet()

    suspend fun close() =
        coroutineScope {
            disconnect()
            selectorManager.close()
        }
}

suspend fun RconClient.executeWithRetry(
    command: String,
    maxAttempts: Int = 3,
    delay: Duration = 1.seconds,
): RconCommandResult {
    var lastResult: RconCommandResult? = null

    repeat(maxAttempts) { attempt ->
        val result = executeCommand(command)
        if (result.success) return result

        lastResult = result
        if (attempt < maxAttempts - 1) {
            kotlinx.coroutines.delay(delay)
        }
    }

    return lastResult ?: RconCommandResult(
        success = false,
        response = "",
        executionTimeMs = 0,
        error = "Max attempts reached",
    )
}
