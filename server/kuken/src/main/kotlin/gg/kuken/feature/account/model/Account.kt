package gg.kuken.feature.account.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Account
    @OptIn(ExperimentalUuidApi::class)
    constructor(
        val id: Uuid,
        val email: String,
        val displayName: String?,
        val createdAt: Instant,
        val updatedAt: Instant,
        val lastLoggedInAt: Instant?,
        val avatar: String?,
    )
