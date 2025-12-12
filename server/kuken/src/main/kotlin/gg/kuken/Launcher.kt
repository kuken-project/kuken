package gg.kuken

import gg.kuken.core.CompositeEventDispatcher
import gg.kuken.core.EventDispatcher
import gg.kuken.core.security.BcryptHash
import gg.kuken.core.security.Hash
import gg.kuken.feature.account.AccountDI
import gg.kuken.feature.account.IdentityGeneratorService
import gg.kuken.feature.auth.AuthDI
import gg.kuken.feature.blueprint.BlueprintDI
import gg.kuken.feature.instance.InstancesDI
import gg.kuken.feature.unit.UnitDI
import gg.kuken.http.Http
import gg.kuken.orchestrator.Orchestrator
import gg.kuken.orchestrator.RedisEventDispatcher
import jakarta.validation.Validation
import jakarta.validation.Validator
import kotlinx.coroutines.runBlocking
import me.devnatan.dockerkt.DockerClient
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main() {
    val config = loadConfig()
    if (config.devMode) {
        setupDevMode()
    }

    val di = configureDependencyInjection(config)
    runBlocking {
        val database = di.koin.get<Database>()
        checkDatabaseConnection(database)

        Http(config).start()
    }
}

private fun configureDependencyInjection(config: KukenConfig) =
    startKoin {
        val root =
            module {
                single { config }

                single { DatabaseFactory(config).create() }

                single { setupRedis(config.redis) }

                single {
                    DockerClient {
                        debugHttpCalls(debugHttpCalls = config.devMode)
                    }
                }

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
                    CompositeEventDispatcher(
                        dispatchers =
                            listOf(
                                RedisEventDispatcher(redisClient = get()),
                            ),
                    )
                }

                single<Orchestrator> {
                    Orchestrator(redisClient = get())
                }
            }

        modules(root, AccountDI, AuthDI, InstancesDI, BlueprintDI, UnitDI)
    }
