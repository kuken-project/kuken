package gg.kuken.core.security

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class SensitiveText(
    val sensitiveValue: String,
) {
    override fun toString(): String = "<redacted>"
}
