package gg.kuken.feature.blueprint

import gg.kuken.feature.blueprint.parser.BlueprintParser
import gg.kuken.feature.blueprint.parser.Property
import kotlinx.serialization.json.JsonObject

internal inline fun <R> withParserTest(crossinline block: BlueprintParser.() -> R): R = block(BlueprintParser())

internal inline fun <R> withParserTest(
    vararg supportedProperties: Property,
    crossinline block: BlueprintParser.() -> R,
): R = block(BlueprintParser(supportedProperties.toList()))

fun withParserTest(
    input: String,
    vararg supportedProperties: Property,
): JsonObject = BlueprintParser(supportedProperties.toList()).read(input)
