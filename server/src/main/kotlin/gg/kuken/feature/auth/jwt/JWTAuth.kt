

package gg.kuken.feature.auth.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.MissingClaimException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
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
    private val hashAlgorithm: Hash,
) : AuthService {
    companion object {
        private val jwtTokenLifetime: Duration = 6.hours
        private const val JWT_ISSUER = "Kuken"
    }

    private val algorithm = Algorithm.HMAC256("michjaelJackson")

    private fun validate(
        input: CharArray,
        hash: String,
    ): Boolean {
        if (input.isEmpty() && hash.isEmpty()) {
            return true
        }

        return runCatching {
            hashAlgorithm.compare(input, hash)
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
                .sign(algorithm)
        } catch (_: JWTCreationException) {
            throw InvalidAccessTokenException()
        }
    }

    override suspend fun verify(subject: String?): Account? {
        val id = subject?.let(Uuid::parseHex) ?: error("Malformed UUID: $subject")
        return accountService.getAccount(id)
    }
}

class JWTVerifierImpl : JWTVerifier {
    companion object {
        private const val JWT_ISSUER = "Kuken"
    }

    // TODO generate secret
    private val algorithm = Algorithm.HMAC256("michjaelJackson")

    private val jwtVerifier: com.auth0.jwt.JWTVerifier =
        JWT
            .require(algorithm)
            .withIssuer(JWT_ISSUER)
            .build()

    private fun internalVerify(token: String): DecodedJWT =
        try {
            jwtVerifier.verify(token)
        } catch (e: JWTVerificationException) {
            val message =
                when (e) {
                    is TokenExpiredException -> "Token has expired"
                    is SignatureVerificationException -> "Invalid signature"
                    is AlgorithmMismatchException -> "Signature algorithm doesn't match"
                    is MissingClaimException -> "Missing JWT claim"
                    else -> null
                }

            error("AuthenticationException(message, e)")
        }

    override fun verify(token: String): DecodedJWT =
        try {
            internalVerify(token)
        } catch (e: Throwable) {
            // TODO Replace Throwable by authentication exception
            throw JWTVerificationException("Access token verification failed", e)
        }

    override fun verify(jwt: DecodedJWT): DecodedJWT =
        try {
            internalVerify(jwt.token)
        } catch (e: Throwable) {
            // TODO Replace Throwable by authentication exception
            throw JWTVerificationException("Access token verification failed", e)
        }
}
