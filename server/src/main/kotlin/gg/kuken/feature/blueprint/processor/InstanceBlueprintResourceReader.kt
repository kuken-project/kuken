package gg.kuken.feature.blueprint.processor

import org.pkl.core.resource.ResourceReader
import java.net.URI
import java.util.Optional

class InstanceBlueprintResourceReader(
    val inputValues: Map<String, String>,
    val refProvider: (key: ResolvedBlueprintRefs) -> Any?,
) : ResourceReader {
    private companion object {
        val REGEX = Regex("__(\\w+)__\\w+:([\\w.]+)__")
    }

    override fun getUriScheme(): String = "kuken"

    override fun read(uri: URI): Optional<in Any> {
        val match = REGEX.find(uri.toString()) ?: return Optional.empty()
        val value: String? =
            when (val kind = match.groupValues[1]) {
                "REF" -> replaceReference(match.groupValues[2])
                else -> error("Unsupported dynamic value replacement kind: $kind")
            }

        return value?.let(Optional<String>::of) ?: Optional.empty()
    }

    private fun replaceReference(key: String): String =
        ResolvedBlueprintRefs.entries
            .firstOrNull { it.key == key }
            ?.let { refProvider(it)?.toString() }
            ?: error("Unsupported reference key for replacement: $key")

    override fun hasHierarchicalUris(): Boolean = false

    override fun isGlobbable(): Boolean = false
}
