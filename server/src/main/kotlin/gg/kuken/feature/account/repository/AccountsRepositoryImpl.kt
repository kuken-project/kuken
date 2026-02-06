package gg.kuken.feature.account.repository

import gg.kuken.feature.account.entity.AccountEntity
import gg.kuken.feature.account.entity.AccountTable
import gg.kuken.feature.account.model.Account
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

class AccountsRepositoryImpl(
    private val database: Database,
) : AccountRepository {
    init {
        transaction(db = database) {
            @Suppress("DEPRECATION")
            SchemaUtils.createMissingTablesAndColumns(AccountTable)
        }
    }

    override suspend fun findAll(): List<AccountEntity> =
        suspendTransaction(db = database, readOnly = true) {
            AccountEntity.all().notForUpdate().toList()
        }

    override suspend fun findById(id: Uuid): AccountEntity? =
        suspendTransaction(db = database, readOnly = true) {
            AccountEntity.findById(id.toJavaUuid())
        }

    override suspend fun findByEmail(email: String): AccountEntity? =
        suspendTransaction(db = database, readOnly = true) {
            AccountEntity
                .find {
                    AccountTable.email eq email
                }.firstOrNull()
        }

    override suspend fun findHashByEmail(email: String): String? =
        suspendTransaction(db = database, readOnly = true) {
            AccountEntity
                .find {
                    AccountTable.email eq email
                }.firstOrNull()
                ?.hash
        }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun addAccount(
        account: Account,
        hash: String,
    ) {
        suspendTransaction(db = database) {
            AccountEntity.new(account.id.toJavaUuid()) {
                this.email = account.email
                this.hash = hash
                this.createdAt = account.createdAt
                this.updatedAt = account.updatedAt
                this.lastLoggedInAt = account.lastLoggedInAt
            }
        }
    }

    override suspend fun deleteAccount(id: Uuid) {
        suspendTransaction(db = database) {
            AccountEntity.findById(id.toJavaUuid())?.delete()
        }
    }

    override suspend fun existsByEmail(email: String): Boolean =
        suspendTransaction(db = database, readOnly = true) {
            !AccountEntity.find { AccountTable.email eq email }.empty()
        }

    override suspend fun existsAnyAccount(): Boolean =
        suspendTransaction(database, readOnly = true) {
            !AccountTable.select(AccountTable.id).fetchSize(1).empty()
        }
}
