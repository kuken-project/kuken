package gg.kuken.feature.blueprint.model

import com.typesafe.config.Config
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.hocon.HoconDecoder
import kotlinx.serialization.json.JsonDecoder

@Serializable
data class BlueprintSpec(
    val name: String,
    val version: String,
    val remote: BlueprintSpecRemote?,
    val build: BlueprintSpecBuild?,
    val options: List<BlueprintSpecOption> = emptyList(),
)

@Serializable
data class BlueprintSpecOption(
    val id: String,
    val name: String,
    val type: List<String>,
    val env: String?,
    val defaultValue: String,
)

@Serializable
data class BlueprintSpecRemote(
    val origin: String,
    val assets: Assets?,
) {
    @Serializable
    data class Assets(
        val iconUrl: String,
    )
}

sealed class ResolvableConfigValue {
    data class Constant(
        val value: String,
    ) : ResolvableConfigValue()

    data class Placeholder(
        val type: Type,
    ) : ResolvableConfigValue() {
        enum class Type(
            val substitution: String,
        ) {
            COMMAND_TEMPLATE("command"),
            SERVER_PORT("port"),
        }
    }
}

@Serializable
data class BlueprintSpecBuild(
    val image: BlueprintSpecImage,
    val entrypoint: String,
    @Serializable(with = EnvKVSerializer::class)
    val env: Map<String, String> = emptyMap(),
    @Transient val instance: BlueprintSpecInstance? = null,
) {
    sealed class EnvironmentVariable {
        data class Constant(
            val value: String,
        ) : EnvironmentVariable()

        data class InputReplacement(
            val inputName: String,
        ) : EnvironmentVariable()
    }

    class EnvKVSerializer : KSerializer<Map<String, Any>> by MapSerializer(String.serializer(), EnvValueSerializer())

    class EnvValueSerializer : KSerializer<Any> {
        @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor =
            buildSerialDescriptor(
                serialName = "gg.kuken.feature.blueprint.model.BlueprintSpecBuild.EnvValueSerializer",
                kind = PolymorphicKind.OPEN,
            )

        @OptIn(ExperimentalSerializationApi::class)
        override fun deserialize(decoder: Decoder): Any {
            // HoconDecoder is used for external blueprints imports
            // StreamingDecoder is used for database blueprint reads
            if (decoder is HoconDecoder) {
                return decoder.decodeConfigValue(Config::getAnyRef).toString()
            }

            decoder as JsonDecoder
            return decoder.decodeJsonElement().toString()
        }

        override fun serialize(
            encoder: Encoder,
            value: Any,
        ) {
            encoder.encodeString(value.toString())
        }
    }
}

@Serializable(with = BlueprintSpecImage.Serializer::class)
sealed class BlueprintSpecImage {
    @Serializable
    @SerialName("identifier")
    data class Identifier(
        val id: String,
    ) : BlueprintSpecImage()

    @Serializable
    @SerialName("ref")
    data class Ref(
        val label: String,
        val tag: String,
    ) : BlueprintSpecImage()

    @Serializable
    @SerialName("multiple")
    data class MultipleIdentifier(
        val images: List<String>,
    ) : BlueprintSpecImage()

    @Serializable
    @SerialName("multiple")
    data class MultipleRef(
        val images: List<Ref>,
    ) : BlueprintSpecImage()

    class Serializer : KSerializer<BlueprintSpecImage> {
        @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
        override val descriptor: SerialDescriptor =
            buildSerialDescriptor(
                serialName = "gg.kuken.feature.blueprint.model.BlueprintSpecImage",
                kind = PolymorphicKind.SEALED,
            )

        override fun deserialize(decoder: Decoder): BlueprintSpecImage =
            try {
                Identifier(decoder.decodeString())
            } catch (_: SerializationException) {
                decoder.decodeStructure(descriptor) {
                    try {
                        val imageList =
                            decodeSerializableElement(
                                descriptor = descriptor,
                                index = 0,
                                deserializer = ListSerializer(String.serializer()),
                            )

                        MultipleIdentifier(imageList)
                    } catch (_: SerializationException) {
                        val imageList =
                            decodeSerializableElement(
                                descriptor = descriptor,
                                index = 0,
                                deserializer = ListSerializer(Ref.serializer()),
                            )

                        MultipleRef(imageList)
                    }
                }
            }

        override fun serialize(
            encoder: Encoder,
            value: BlueprintSpecImage,
        ) = when (value) {
            is Identifier -> {
                encoder.encodeString(value.id)
            }

            is MultipleIdentifier -> {
                encoder.encodeStructure(descriptor) {
                    value.images.forEachIndexed { index, string ->
                        encodeStringElement(descriptor, index, string)
                    }
                }
            }

            is MultipleRef -> {
                encoder.encodeStructure(descriptor) {
                    value.images.forEachIndexed { index, ref ->
                        encodeSerializableElement(descriptor, index, Ref.serializer(), ref)
                    }
                }
            }

            is Ref -> {
                encoder.encodeSerializableValue(Ref.serializer(), value)
            }
        }
    }
}

@Serializable
data class BlueprintSpecInstance(
    val name: String,
)
