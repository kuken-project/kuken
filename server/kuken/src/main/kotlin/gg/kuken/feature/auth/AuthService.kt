package gg.kuken.feature.auth

import gg.kuken.feature.account.model.Account

interface AuthService {
    suspend fun auth(
        username: String,
        password: String,
    ): String

    suspend fun verify(subject: String?): Account?
}
