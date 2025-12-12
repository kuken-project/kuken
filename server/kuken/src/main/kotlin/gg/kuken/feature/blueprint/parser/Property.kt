package gg.kuken.feature.blueprint.parser

import gg.kuken.feature.blueprint.parser.PropertyKind.Mixed
import gg.kuken.feature.blueprint.parser.PropertyKind.Multiple
import kotlin.reflect.KClass

const val PROPERTY_NAME_SEPARATOR = "."

data class Property(
    val qualifiedName: String,
    val kind: PropertyKind,
    val constraints: List<PropertyConstraint> = listOf(),
) {
    val nameStructure: List<String> get() = qualifiedName.split(PROPERTY_NAME_SEPARATOR)

    fun supports(kind: KClass<out PropertyKind>): Boolean = supports(this.kind, kind)

    companion object {
        private fun supports(
            kind: PropertyKind,
            other: KClass<out PropertyKind>,
        ): Boolean =
            when (kind) {
                is Mixed -> kind.isAllTypesSupported || kind.kinds.any { supports(it, other) }
                is Multiple -> supports(kind.supports, other)
                else -> kind::class == other
            }
    }
}

sealed class PropertyKind {
    object Null : PropertyKind()

    object Literal : PropertyKind()

    object Numeric : PropertyKind()

    object TrueOrFalse : PropertyKind()

    data class Struct(
        val allowUnknown: Boolean,
    ) : PropertyKind()

    data class Multiple(
        val supports: PropertyKind,
    ) : PropertyKind()

    data class Mixed(
        val kinds: List<PropertyKind>,
    ) : PropertyKind() {
        val isAllTypesSupported: Boolean = kinds.isEmpty()

        constructor(vararg kinds: PropertyKind) : this(kinds.toList())
    }
}

internal val AllSupportedProperties: List<Property> =
    listOf(
        Properties.Name,
        Properties.Version,
        Properties.Build,
        Properties.Entrypoint,
        Properties.Env,
        Properties.Image,
        Properties.ImageReference,
        Properties.ImageTag,
        Properties.Instance,
        Properties.InstanceName,
        Properties.Options,
        Properties.OptionsId,
        Properties.OptionsType,
        Properties.OptionsEnv,
    )
