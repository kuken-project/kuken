package gg.kuken

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigParseOptions
import io.lettuce.core.RedisClient
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
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
        Database.connect(
            url = "jdbc:postgresql://${appConfig.db.host}",
            user = appConfig.db.user,
            password = appConfig.db.password,
            driver = "org.postgresql.Driver",
            databaseConfig =
                DatabaseConfig {
                    useNestedTransactions = true
                    if (appConfig.devMode) {
                        sqlLogger = Slf4jSqlDebugLogger
                    }
                },
        )
}

suspend fun checkDatabaseConnection(database: Database) {
    try {
        newSuspendedTransaction(db = database, readOnly = true) {
            database.connector()
        }
    } catch (_: SQLException) {
        error("Unable to establish database connection")
    }
}
