package gg.kuken.feature.blueprint

import gg.kuken.core.KukenException
import gg.kuken.feature.blueprint.parser.Property
import gg.kuken.feature.blueprint.parser.PropertyConstraint

open class BlueprintException : KukenException()

class BlueprintNotFoundException : BlueprintException()

class NoMatchingBlueprintSpecProviderException : BlueprintException()

class BlueprintSpecNotFound : BlueprintException()

class BlueprintConflictException : BlueprintException()

class UnsupportedBlueprintSpecSource : BlueprintException()

open class BlueprintSpecParseException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : BlueprintException()

open class BlueprintSpecPropertyParseException(
    val property: Property,
    message: String? = null,
    cause: Throwable? = null,
) : BlueprintSpecParseException(message, cause)

class NoMatchesForMixedProperty(
    message: String?,
    property: Property,
) : BlueprintSpecPropertyParseException(property, message)

class ConstraintViolationException(
    message: String?,
    property: Property,
    val constraint: PropertyConstraint,
) : BlueprintSpecPropertyParseException(property, message)
