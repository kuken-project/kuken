package gg.kuken.http

import gg.kuken.KukenConfig
import gg.kuken.feature.account.http.AccountHttpModule
import gg.kuken.feature.auth.http.AuthHttpModule
import gg.kuken.feature.blueprint.http.BlueprintHttpModule
import gg.kuken.feature.instance.http.InstanceHttpModule
import gg.kuken.feature.remoteConfig.RemoteConfig
import gg.kuken.feature.remoteConfig.RemoteConfigService
import gg.kuken.feature.setup.http.SetupHttpModule
import gg.kuken.feature.unit.http.UnitHttpModule
import gg.kuken.websocket.WebSocketManager
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.addShutdownHook
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import kotlinx.atomicfu.atomic
import kotlinx.io.files.FileNotFoundException
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import kotlin.getValue

private const val STOP_GRACE_PERIOD_MILLIS: Long = 1000
private const val TIMEOUT_MILLIS: Long = 5000

class Http(
    val appConfig: KukenConfig,
    val webSocketManager: WebSocketManager,
) : KoinComponent {
    private val log = LoggerFactory.getLogger(Http::class.java)
    private var shutdownPending = atomic(false)
    private val engine: EmbeddedServer<*, *> = createServer()

    init {
        if (appConfig.devMode) {
            System.setProperty("io.ktor.development", "true")
        }
    }

    suspend fun start() {
        engine.addShutdownHook(::stop)
        engine.startSuspend(wait = true)
    }

    fun stop() {
        if (shutdownPending.compareAndSet(expect = false, update = false)) {
            engine.stop(STOP_GRACE_PERIOD_MILLIS, TIMEOUT_MILLIS)
        }
    }

    private fun Application.configureAPIDocs() {
        routing {
            try {
                openAPI(path = "openapi", swaggerFile = "openapi/generated.json")
                swaggerUI(path = "swagger", swaggerFile = "openapi/generated.json")
            } catch (e: FileNotFoundException) {
                log.error("Could not generate Open API docs: ${e.message}")
            }
        }
    }

    private fun Application.registerHttpModules() {
        if (appConfig.devMode) {
            configureAPIDocs()
        }

        for (module in createHttpModules().sortedByDescending(HttpModule::priority)) {
            module.install(this)

            for ((op, handler) in module.webSocketHandlers()) {
                webSocketManager.register(op, handler)
            }
        }
    }

    private fun configureApplication(app: Application): Unit =
        with(app) {
            installDefaultFeatures(appConfig)
            routing {
                webSocket { webSocketManager.connect(this) }
                get("/") {
                    with(call) { respondServerInfo() }
                }
            }

            registerHttpModules()
        }

    @Serializable
    private data class ServerInfo(
        val organization: Organization,
        val production: Boolean,
        val version: String,
    ) {
        @Serializable
        data class Organization(
            val name: String,
        )
    }

    private suspend fun RoutingCall.respondServerInfo() {
        val remoteConfigService by inject<RemoteConfigService>()
        val needsSetup = !remoteConfigService.isConfigValueSet(RemoteConfig.OrganizationName)
        if (needsSetup) {
            response.status(HttpStatusCode.Locked)
            return
        }

        respond(
            ServerInfo(
                organization =
                    ServerInfo.Organization(
                        name = remoteConfigService.getConfigValue(RemoteConfig.OrganizationName),
                    ),
                production = !appConfig.devMode,
                version = "0.0.1",
            ),
        )
    }

    private fun createServer() =
        embeddedServer(
            factory = CIO,
            host = appConfig.http.host,
            port = appConfig.http.port,
            watchPaths = listOf("classes").takeIf { appConfig.devMode }.orEmpty(),
            module = { configureApplication(this) },
        )

    fun createHttpModules() =
        setOf(
            AuthHttpModule,
            AccountHttpModule,
            InstanceHttpModule,
            BlueprintHttpModule,
            UnitHttpModule,
            SetupHttpModule,
        )
}
