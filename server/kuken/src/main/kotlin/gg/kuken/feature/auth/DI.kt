package gg.kuken.feature.auth

import com.auth0.jwt.interfaces.JWTVerifier
import gg.kuken.feature.auth.jwt.JWTAuthServiceImpl
import gg.kuken.feature.auth.jwt.JWTVerifierImpl
import org.koin.dsl.module

val AuthDI =
    module {
        single<AuthService> { JWTAuthServiceImpl(get(), get()) }
        single<JWTVerifier> { JWTVerifierImpl() }
    }
