package gg.kuken.feature.auth.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import gg.kuken.core.security.Hash
import gg.kuken.feature.account.AccountService
import gg.kuken.feature.account.http.exception.AccountNotFoundException
import gg.kuken.feature.account.model.Account
import gg.kuken.feature.auth.AuthService
import gg.kuken.feature.auth.http.exception.InvalidAccessTokenException
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaInstant
import kotlin.uuid.Uuid

class JWTAuthServiceImpl(
    private val accountService: AccountService,
    private val passwordHashAlgorithm: Hash,
    private val tokenAlgorithmFactory: AlgorithmFactory,
) : AuthService {
    companion object {
        private val jwtTokenLifetime: Duration = 6.hours
        private const val JWT_ISSUER = "Kuken"
    }

    private val tokenAlgorithm: Algorithm by lazy { tokenAlgorithmFactory.create() }

    private fun validate(
        input: CharArray,
        hash: String,
    ): Boolean {
        if (input.isEmpty() && hash.isEmpty()) {
            return true
        }

        return runCatching {
            passwordHashAlgorithm.compare(input, hash)
        }.recoverCatching { exception ->
            throw SecurityException("Could not decrypt data.", exception)
        }.getOrThrow()
    }

    override suspend fun auth(
        username: String,
        password: String,
    ): String {
        val (account, hash) =
            accountService.getAccountAndHash(username)
                ?: throw AccountNotFoundException()

        val validated =
            validate(
                input = password.toCharArray(),
                hash = hash,
            )
        if (!validated) throw InvalidAccessTokenException()

        val now = Clock.System.now()
        return try {
            JWT
                .create()
                .withIssuedAt(now.toJavaInstant())
                .withIssuer(JWT_ISSUER)
                .withExpiresAt(now.plus(jwtTokenLifetime).toJavaInstant())
                .withSubject(account.id.toHexString())
                .sign(tokenAlgorithm)
        } catch (_: JWTCreationException) {
            throw InvalidAccessTokenException()
        }
    }

    override suspend fun verify(subject: String?): Account? {
        val id = subject?.let(Uuid::parseHex) ?: error("Malformed UUID: $subject")
        return accountService.getAccount(id)
    }
}
