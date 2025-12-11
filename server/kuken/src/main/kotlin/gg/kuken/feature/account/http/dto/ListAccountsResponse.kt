package gg.kuken.feature.account.http.dto

import gg.kuken.feature.account.model.Account
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class AccountResponse(
    @SerialName("accountId") val id: String,
    @SerialName("displayName") val displayName: String?,
    @SerialName("email") val email: String,
    @SerialName("createdAt") val createdAt: Instant,
    @SerialName("updatedAt") val updatedAt: Instant,
) {
    constructor(account: Account) : this(
        id = account.id.toString(),
        displayName = account.displayName,
        email = account.email,
        createdAt = account.createdAt,
        updatedAt = account.updatedAt,
    )
}
