package gg.kuken

import kotlinx.serialization.Serializable

@Serializable
data class KukenConfig(val http: HttpConfig, val db: DBConfig, val redis: RedisConfig) {

    val devMode: Boolean = System.getenv("PRODUCTION")?.toBoolean() ?: false

    @Serializable
    data class HttpConfig(
        val host: String,
        val port: Int,
    )

    @Serializable
    data class DBConfig(
        val host: String,
        val user: String,
        val password: String,
    )

    @Serializable
    data class RedisConfig(val url: String)
}