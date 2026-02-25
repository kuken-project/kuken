package gg.kuken

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigParseOptions
import io.lettuce.core.RedisClient
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.Slf4jSqlDebugLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.io.File
import java.sql.SQLException

fun loadConfig(): KukenConfig {
    val parseOptions = ConfigParseOptions.defaults().setAllowMissing(true)
    val config =
        ConfigFactory
            .parseResources("kuken.local.conf")
            .withFallback(ConfigFactory.parseFile(File("kuken.conf"), parseOptions))
            .withFallback(ConfigFactory.parseResources("kuken.conf", parseOptions))
            .resolve()

    @OptIn(ExperimentalSerializationApi::class)
    return Hocon {}.decodeFromConfig(config)
}

fun setupDevMode() {
    System.setProperty(
        kotlinx.coroutines.DEBUG_PROPERTY_NAME,
        kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON,
    )
}

fun setupRedis(config: KukenConfig.RedisConfig): RedisClient {
    val client = RedisClient.create(config.url)
    Runtime.getRuntime().addShutdownHook(Thread { client.shutdown() })

    return client
}

class DatabaseFactory(
    private val appConfig: KukenConfig,
) {
    fun create(): Database =
        Database
            .connect(
                url = appConfig.db.url,
                user = appConfig.db.user,
                password = appConfig.db.password,
                databaseConfig =
                    DatabaseConfig {
                        useNestedTransactions = true
                        if (appConfig.devMode) {
                            sqlLogger = Slf4jSqlDebugLogger
                        }
                    },
            ).also { TransactionManager.defaultDatabase = it }
}

suspend fun checkDatabaseConnection(database: Database) {
    try {
        suspendTransaction(db = database, readOnly = true) {
            database.connector()
        }
    } catch (_: SQLException) {
        error("Unable to establish database connection")
    }
}
