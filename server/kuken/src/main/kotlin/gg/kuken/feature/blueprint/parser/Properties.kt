package gg.kuken.feature.blueprint.parser

import gg.kuken.feature.blueprint.parser.PropertyKind.Literal
import gg.kuken.feature.blueprint.parser.PropertyKind.Mixed
import gg.kuken.feature.blueprint.parser.PropertyKind.Multiple
import gg.kuken.feature.blueprint.parser.PropertyKind.Struct

internal object Properties {
    val Name =
        Property(
            qualifiedName = "name",
            kind = Literal,
            constraints = listOf(RequiredPropertyConstraint, NotBlankPropertyConstraint),
        )
    val Version =
        Property(
            qualifiedName = "version",
            kind = Literal,
            constraints = listOf(RequiredPropertyConstraint, NotBlankPropertyConstraint),
        )
    val Build =
        Property(
            qualifiedName = "build",
            kind = Struct(allowUnknown = false),
        )
    val Entrypoint =
        Property(
            qualifiedName = "build.entrypoint",
            kind = Literal,
        )
    val Image =
        Property(
            qualifiedName = "build.image",
            kind =
                Mixed(
                    Literal,
                    Multiple(Struct(allowUnknown = false)),
                ),
            constraints = listOf(RequiredPropertyConstraint),
        )

    val Env =
        Property(
            qualifiedName = "build.env",
            kind = Struct(allowUnknown = true),
        )

    val ImageReference =
        Property(
            qualifiedName = "build.image.ref",
            kind = Literal,
            constraints = listOf(RequiredPropertyConstraint),
        )
    val ImageTag =
        Property(
            qualifiedName = "build.image.tag",
            kind = Literal,
            constraints = listOf(RequiredPropertyConstraint),
        )
    val Instance =
        Property(
            qualifiedName = "build.instance",
            kind = Struct(allowUnknown = false),
        )
    val InstanceName =
        Property(
            qualifiedName = "build.instance.name",
            kind = Literal,
        )
    val Options =
        Property(
            qualifiedName = "options",
            kind = Struct(allowUnknown = false),
        )
    val OptionsId =
        Property(
            qualifiedName = "options.id",
            kind = Literal,
            constraints = listOf(RequiredPropertyConstraint),
        )
    val OptionsType =
        Property(
            qualifiedName = "options.type",
            kind = Literal,
            constraints = listOf(RequiredPropertyConstraint),
        )
    val OptionsEnv =
        Property(
            qualifiedName = "options.env",
            kind = Literal,
            constraints = listOf(EnvironmentVariableConstraint),
        )
}
