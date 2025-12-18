package gg.kuken.feature.blueprint.parser

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun JsonObject.string(key: String) = getValue(key).jsonPrimitive.contentOrNull.orEmpty()

fun JsonObject.struct(key: String) = this[key]?.jsonObject
