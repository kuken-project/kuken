@file:OptIn(ExperimentalUuidApi::class)

package gg.kuken.feature.account.entity

import gg.kuken.feature.account.model.Account
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

class AccountEntity(
    id: EntityID<UUID>,
) : UUIDEntity(id) {
    internal companion object : UUIDEntityClass<AccountEntity>(AccountTable)

    var email by AccountTable.email
    var displayName by AccountTable.displayName
    var hash by AccountTable.hash
    var createdAt by AccountTable.createdAt
    var updatedAt by AccountTable.updatedAt
    var lastLoggedInAt by AccountTable.lastLoggedInAt
    val avatar by AccountTable.avatar
}

object AccountTable : UUIDTable("accounts") {
    val email = varchar("email", length = 255)
    val displayName = varchar("display_name", length = 255).nullable()
    val hash = varchar("hash", length = 255)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val lastLoggedInAt = timestamp("last_logged_in_at").nullable()
    val avatar = long("avatar").nullable()
}

fun AccountEntity.toDomain(): Account =
    Account(
        id = id.value.toKotlinUuid(),
        email = email,
        displayName = displayName,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastLoggedInAt = lastLoggedInAt,
        avatar = avatar?.toString(),
    )
