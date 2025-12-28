package gg.kuken.feature.auth.jwt

import com.auth0.jwt.algorithms.Algorithm

class AlgorithmProvider {
    private val algorithm by lazy {
        val secret =
            System.getenv("JWT_SECRET") ?: java.util.UUID
                .randomUUID()
                .toString()
        Algorithm.HMAC256(secret)
    }

    fun get(): Algorithm = algorithm
}
