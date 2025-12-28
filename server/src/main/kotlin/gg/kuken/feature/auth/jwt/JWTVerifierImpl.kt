package gg.kuken.feature.auth.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.MissingClaimException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier

class JWTVerifierImpl(
    tokenAlgorithmFactory: AlgorithmFactory,
) : JWTVerifier {
    companion object {
        private const val JWT_ISSUER = "Kuken"
    }

    private val jwtVerifier: com.auth0.jwt.JWTVerifier =
        JWT
            .require(tokenAlgorithmFactory.create())
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

            error("AuthenticationException($message, $e)")
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
