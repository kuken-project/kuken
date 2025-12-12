package gg.kuken.feature.instance

import gg.kuken.feature.instance.model.HostPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import me.devnatan.dockerkt.DockerClient
import me.devnatan.dockerkt.DockerException
import me.devnatan.dockerkt.models.network.Network
import me.devnatan.dockerkt.resource.NetworkNotFoundException
import me.devnatan.dockerkt.resource.network.create
import org.apache.logging.log4j.LogManager
import java.net.ServerSocket

class DockerNetworkService(
    private val dockerClient: DockerClient,
) {
    companion object {
        private const val MACVLAN_DRIVER = "macvlan"
        internal const val HOST_DRIVER = "host"
    }

    private val logger = LogManager.getLogger(DockerNetworkService::class.java)

    suspend fun connect(
        network: String,
        container: String,
    ) {
        val network =
            runCatching {
                withContext(IO) { dockerClient.networks.inspect(network) }
            }.recoverCatching {
                tryCreateNetwork(network)
            }.getOrThrow()

        if (network.isInternal) {
            throw InvalidNetworkAssignmentException("Internal networks cannot be connected to")
        }

        when (network.driver) {
            HOST_DRIVER -> {
                logger.warn(
                    "We recommend that the network of the created instances is not externally " +
                        "accessible, the network being used ({}) is of the \"host\" type, which " +
                        "exposes the connection of the instances to anyone who wants to access them.",
                    network.name,
                )
            }

            MACVLAN_DRIVER -> {
                applyMacvlanIpAddress()
            }
        }

        try {
            withContext(IO) {
                dockerClient.networks.connectContainer(
                    id = network.id,
                    container = container,
                )
            }
        } catch (exception: DockerException) {
            throw NetworkConnectionFailed(
                network = network.id,
                cause = exception,
            )
        }
    }

    /**
     * Creates a new connection using the specified host and port. If the host is null, it defaults to
     * listening on all network interfaces. If the port is null, a random available port is selected.
     *
     * @param host The hostname or IP address to bind to.
     * @param port The port number to bind to. If null, a random available port is selected.
     * @return An instance of [HostPort] containing the resolved host and port configuration.
     */
    suspend fun createAddress(
        host: String?,
        port: UShort?,
    ): HostPort =
        HostPort(
            host = host,
            port = port ?: randomPort(),
        )

    /**
     * Selects a random, currently available network port and returns it.
     *
     * @return A randomly selected, available port number.
     */
    private suspend fun randomPort(): UShort =
        withContext(IO) {
            ServerSocket(0)
                .use { socket ->
                    socket.localPort
                }.toUShort()
        }

    /**
     * Tries to create a network with the given [name] returning the newly created [Network].
     *
     * @param name The network name.
     * @throws UnknownNetworkException If network couldn't be found.
     */
    private suspend fun tryCreateNetwork(name: String): Network {
        val created = createNetwork(name)
        return try {
            dockerClient.networks.inspect(name)
        } catch (_: NetworkNotFoundException) {
            throw UnknownNetworkException(created)
        }
    }

    /**
     * Creates a new network with the given [name] and returns its id.
     *
     * @param name The network name.
     */
    private suspend fun createNetwork(name: String): String {
        logger.debug("Creating network {}...", name)
        return dockerClient.networks.create { this.name = name }.id
    }

    /**
     * By default, Docker always use the 1st interface for outbound traffic and can lead to issues
     * like being unable to use two ip addresses on the same none.
     *
     * Macvlan network driver allows IP addresses to be manually assigned to the container so that
     * the container does not use random IPs.
     */
    @Suppress("RedundantSuspendModifier")
    private suspend fun applyMacvlanIpAddress() {
        // TODO check ip address conflicts and apply proper NetworkingConfig
        // https://docs.docker.com/engine/api/v1.41/#tag/Container/operation/ContainerCreate

        throw InvalidNetworkAssignmentException("Macvlan network is not supported")
    }
}
