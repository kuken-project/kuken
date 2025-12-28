package gg.kuken.feature.auth

import com.auth0.jwt.interfaces.JWTVerifier
import gg.kuken.feature.auth.jwt.AlgorithmProvider
import gg.kuken.feature.auth.jwt.JWTAuthServiceImpl
import gg.kuken.feature.auth.jwt.JWTVerifierImpl
import org.koin.dsl.module

val AuthDI =
    module {
        single<AlgorithmProvider> {
            AlgorithmProvider()
        }

        single<AuthService> {
            JWTAuthServiceImpl(
                accountService = get(),
                passwordHashAlgorithm = get(),
                tokenAlgorithmProvider = get(),
            )
        }

        single<JWTVerifier> {
            JWTVerifierImpl(
                tokenAlgorithmProvider = get(),
            )
        }
    }
