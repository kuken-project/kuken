package gg.kuken.feature.blueprint

import gg.kuken.feature.blueprint.processor.BlueprintResolutionContext
import gg.kuken.feature.blueprint.processor.CheckboxInput
import gg.kuken.feature.blueprint.processor.DataSizeInput
import gg.kuken.feature.blueprint.processor.PasswordInput
import gg.kuken.feature.blueprint.processor.PortInput
import gg.kuken.feature.blueprint.processor.Resolvable
import gg.kuken.feature.blueprint.processor.ResolvedBlueprint
import gg.kuken.feature.blueprint.processor.ResolvedBlueprintRefs
import gg.kuken.feature.blueprint.processor.SelectInput
import gg.kuken.feature.blueprint.processor.TextInput
import kotlinx.serialization.InternalSerializationApi
import org.slf4j.LoggerFactory

/**
 * Resolves blueprint property references and performs template substitutions.
 *
 * Handles various types of resolvable properties including environment variables,
 * inputs, runtime references, and interpolated templates.
 */
class BlueprintPropertyResolver {
    private val logger = LoggerFactory.getLogger(BlueprintPropertyResolver::class.java)

    /**
     * Resolves a resolvable property to its string value.
     *
     * @param property The property to resolve
     * @param blueprint The resolved blueprint containing input definitions
     * @param context The resolution context with runtime values
     * @return The resolved value or null if the property cannot be resolved
     */
    @OptIn(InternalSerializationApi::class)
    fun resolve(
        property: Resolvable<*>,
        blueprint: ResolvedBlueprint,
        context: BlueprintResolutionContext,
    ): String? =
        when (property) {
            is Resolvable.EnvVarRef -> {
                context.env[property.envVarName].orEmpty()
            }

            is Resolvable.ConditionalRef -> {
                null
            }

            is Resolvable.InputRef -> {
                val definition = blueprint.inputs.firstOrNull { it.name == property.inputName }
                val defaultValue: String? =
                    when (definition) {
                        is CheckboxInput -> {
                            definition.default.toString()
                        }

                        is DataSizeInput -> {
                            null
                        }

                        is PasswordInput -> {
                            null
                        }

                        is PortInput -> {
                            (definition.default ?: false).toString()
                        }

                        is SelectInput -> {
                            null
                        }

                        is TextInput -> {
                            null
                        }

                        null -> {
                            null
                        }
                    }

                val input =
                    context.inputs[property.inputName]
                        ?: defaultValue
                        ?: error("Missing required input ${property.inputName}")

                input
            }

            is Resolvable.Interpolated -> {
                logger.debug("Resolving dynamic property...: ${property.template}")

                check(property.parts.isNotEmpty()) {
                    "Dynamic property provided but no dependencies found"
                }

                var literal = property.template
                property.parts
                    .groupBy { it.toTemplateString() }
                    .forEach { (template, dependencies) ->
                        check(dependencies.size == 1) {
                            "Multiple dependencies are not supported"
                        }

                        for (dependency in dependencies) {
                            val substitution =
                                resolve(dependency, blueprint, context)
                                    ?: continue

                            literal = literal.replace(template, substitution)
                        }
                    }

                literal
            }

            is Resolvable.Literal -> {
                if (property.value == null.toString()) null else property.value
            }

            is Resolvable.RuntimeRef -> {
                val refT = ResolvedBlueprintRefs.entries.first { it.key == property.refPath }
                when (refT) {
                    ResolvedBlueprintRefs.INSTANCE_ID -> context.instanceId.toString()
                    ResolvedBlueprintRefs.INSTANCE_NAME -> context.instanceName
                    ResolvedBlueprintRefs.INSTANCE_MEMORY -> "1024"
                    ResolvedBlueprintRefs.NETWORK_HOST -> context.address.host.toString()
                    ResolvedBlueprintRefs.NETWORK_PORT -> context.address.port.toString()
                }
            }

            Resolvable.Null -> {
                null
            }
        }
}
