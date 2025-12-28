package gg.kuken.feature.auth.jwt

import com.auth0.jwt.algorithms.Algorithm

class AlgorithmFactory {
    fun create(): Algorithm {
        val secret =
            System.getenv("JWT_SECRET") ?: java.util.UUID
                .randomUUID()
                .toString()
        return Algorithm.HMAC256(secret)
    }
}
