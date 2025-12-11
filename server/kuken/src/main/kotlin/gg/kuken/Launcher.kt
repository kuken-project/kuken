package gg.kuken

import gg.kuken.core.CompositeEventDispatcher
import gg.kuken.core.EventDispatcher
import gg.kuken.core.security.BcryptHash
import gg.kuken.core.security.Hash
import gg.kuken.feature.account.AccountDI
import gg.kuken.feature.account.IdentityGeneratorService
import gg.kuken.feature.auth.AuthDI
import gg.kuken.feature.instance.InstancesDI
import gg.kuken.http.Http
import gg.kuken.orchestrator.Orchestrator
import gg.kuken.orchestrator.RedisEventDispatcher
import io.lettuce.core.RedisClient
import jakarta.validation.Validation
import jakarta.validation.Validator
import kotlinx.coroutines.runBlocking
import me.devnatan.dockerkt.DockerClient
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.jetbrains.exposed.sql.Database
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal fun main() {
    val config = loadConfig()
    if (config.devMode) {
        setupDevMode()
    }

    val database =
        runBlocking {
            DatabaseFactory(config).create().also { db -> checkDatabaseConnection(db) }
        }

    val docker =
        DockerClient {
            debugHttpCalls(debugHttpCalls = config.devMode)
        }

    val redis = setupRedis(config.redis)
    configureDI(config, database, redis, docker)

    runBlocking {
        Http(config).start()
    }
}

private fun configureDI(
    config: KukenConfig,
    db: Database,
    redis: RedisClient,
    docker: DockerClient,
) {
    startKoin {
        val root =
            module {
                single(createdAtStart = true) { config }
                single(createdAtStart = true) { db }
                single(createdAtStart = true) { redis }
                single(createdAtStart = true) { docker }

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

        modules(root, AccountDI, AuthDI, InstancesDI)
    }
}
