package gg.kuken.feature.blueprint.processor

import gg.kuken.feature.blueprint.model.BlueprintHeader
import org.pkl.core.Composite
import org.pkl.core.PModule

fun PModule.toBlueprintHeader() =
    BlueprintHeader(
        name = getProperty("name") as String,
        version = getProperty("version") as String,
        url = getProperty("url") as String,
        author = getProperty("author") as String,
    )

inline operator fun <reified T> Composite.get(path: String): T {
    val prop = getPropertyOrNull(path) ?: return null as T
    return prop as T
}
