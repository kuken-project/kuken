package gg.kuken

import gg.kuken.core.EventDispatcher
import gg.kuken.core.ResourceIdFactory
import gg.kuken.core.docker.DockerEventDispatcher
import gg.kuken.core.security.BcryptHash
import gg.kuken.core.security.Hash
import gg.kuken.feature.account.AccountDI
import gg.kuken.feature.account.IdentityGeneratorService
import gg.kuken.feature.auth.AuthDI
import gg.kuken.feature.blueprint.BlueprintDI
import gg.kuken.feature.instance.ActivityLogStore
import gg.kuken.feature.instance.InstancesDI
import gg.kuken.feature.rbac.RBACDI
import gg.kuken.feature.remoteConfig.RemoteConfigDI
import gg.kuken.feature.setup.SetupDI
import gg.kuken.feature.unit.UnitDI
import gg.kuken.http.Http
import gg.kuken.orchestrator.Orchestrator
import gg.kuken.websocket.WebSocketManager
import jakarta.validation.Validation
import jakarta.validation.Validator
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import me.devnatan.dockerkt.DockerClient
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("Kuken")

fun main() {
    val config = loadConfig()
    if (config.devMode) {
        setupDevMode()
    }

    log.info("Data directory: ${config.engine.dataDirectory}")

    val di = configureDependencyInjection(config)
    runBlocking {
        val database = di.koin.get<Database>()
        checkDatabaseConnection(database)

        val webSocketManager = di.koin.get<WebSocketManager>()
        Http(config, webSocketManager).start()
    }
}

private fun initDockerClient(config: KukenConfig): DockerClient {
    val client =
        DockerClient {
            apiVersion(config.docker.apiVersion)
            debugHttpCalls(debugHttpCalls = config.devMode)
        }

    runBlocking {
        client.system.ping()
    }

    return client
}

private fun configureDependencyInjection(config: KukenConfig) =
    startKoin {
        val root =
            module {
                single { config }
                single { DatabaseFactory(config).create() }
                single { setupRedis(config.redis) }
                single { initDockerClient(config = get()) }

                single<Hash> { BcryptHash() }

                single<IdentityGeneratorService> { IdentityGeneratorService() }

                single<Validator> {
                    Validation
                        .byDefaultProvider()
                        .configure()
                        .messageInterpolator(ParameterMessageInterpolator())
                        .buildValidatorFactory()
                        .validator
                }

                single<EventDispatcher>(createdAtStart = true) {
                    DockerEventDispatcher(dockerClient = get())
                }

                single<Orchestrator> {
                    Orchestrator(redisClient = get())
                }

                single {
                    WebSocketManager(json = Json { ignoreUnknownKeys = true })
                }

                factory { ResourceIdFactory() }

                single {
                    val config = get<KukenConfig>()
                    ActivityLogStore(
                        logDir = config.engine.dataDirectory.resolve("activities"),
                    )
                }
            }

        modules(root, AccountDI, AuthDI, InstancesDI, BlueprintDI, UnitDI, SetupDI, RemoteConfigDI, RBACDI)
    }
