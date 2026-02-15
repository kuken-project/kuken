package gg.kuken.core

import kotlin.uuid.Uuid

@JvmInline
value class ResourceId(
    val value: Uuid,
) {
    override fun toString(): String = value.toString()
}

class ResourceIdFactory {
    fun generate() = ResourceId(Uuid.generateV7())
}
