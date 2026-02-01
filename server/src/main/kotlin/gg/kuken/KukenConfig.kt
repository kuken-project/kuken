package gg.kuken

import kotlinx.serialization.Serializable
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@Serializable
data class KukenConfig(
    val http: HttpConfig,
    val db: DBConfig,
    val redis: RedisConfig,
    val node: String,
    val docker: DockerConfig,
) {
    val devMode: Boolean = System.getenv("DEV_MODE")?.toBooleanStrictOrNull() == true

    @Serializable
    data class HttpConfig(
        val host: String,
        val port: Int,
    )

    @Serializable
    data class DBConfig(
        val url: String,
        val user: String,
        val password: String,
    )

    @Serializable
    data class RedisConfig(
        val url: String,
    )

    @Serializable
    data class DockerConfig(
        val network: Network,
    ) {
        @Serializable
        data class Network(
            val name: String,
        )
    }

    companion object {
        @JvmStatic
        fun tempDir(vararg path: String): Path = Files.createTempDirectory(path.joinToString(File.separator))
    }
}
