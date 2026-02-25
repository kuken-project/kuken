package gg.kuken.core.database

import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

suspend fun <T> dbQuery(
    readOnly: Boolean? = null,
    statement: suspend JdbcTransaction.() -> T,
) = suspendTransaction(readOnly = readOnly, statement = statement)
