package gg.kuken.feature.remoteConfig

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert

object RemoteConfigTable : Table("config") {
    val key = varchar("key", length = 255)
    val value = varchar("value", length = 255)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(key)
}

class RemoteConfigRepository(
    private val database: Database,
) {
    init {
        transaction {
            @Suppress("DEPRECATION")
            SchemaUtils.createMissingTablesAndColumns(RemoteConfigTable)
        }
    }

    suspend fun findConfigValue(key: String): String? =
        suspendTransaction(readOnly = true) {
            RemoteConfigTable
                .select(RemoteConfigTable.key, RemoteConfigTable.value)
                .where { RemoteConfigTable.key eq key }
                .map { it[RemoteConfigTable.value] }
                .singleOrNull()
        }

    fun updateConfigValue(
        key: String,
        value: String,
    ) = RemoteConfigTable.upsert {
        it[RemoteConfigTable.key] = key
        it[RemoteConfigTable.value] = value
    }

    suspend fun existsConfigValue(key: String): Boolean =
        suspendTransaction(db = database, readOnly = true) {
            RemoteConfigTable
                .select(RemoteConfigTable.key, RemoteConfigTable.value)
                .where { RemoteConfigTable.key eq key }
                .limit(1)
                .empty()
                .not()
        }
}
