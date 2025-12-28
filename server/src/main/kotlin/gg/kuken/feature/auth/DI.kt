package gg.kuken.feature.auth

import com.auth0.jwt.interfaces.JWTVerifier
import gg.kuken.feature.auth.jwt.AlgorithmFactory
import gg.kuken.feature.auth.jwt.JWTAuthServiceImpl
import gg.kuken.feature.auth.jwt.JWTVerifierImpl
import org.koin.dsl.module

val AuthDI =
    module {
        single<AlgorithmFactory> {
            AlgorithmFactory()
        }

        single<AuthService> {
            JWTAuthServiceImpl(
                accountService = get(),
                passwordHashAlgorithm = get(),
                tokenAlgorithmFactory = get(),
            )
        }

        single<JWTVerifier> {
            JWTVerifierImpl(
                tokenAlgorithmFactory = get(),
            )
        }
    }
