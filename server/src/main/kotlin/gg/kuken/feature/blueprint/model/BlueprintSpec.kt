package gg.kuken.feature.blueprint.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

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

@Serializable
data class BlueprintSpecBuild(
    val image: BlueprintSpecImage,
    val entrypoint: String,
    @Transient val env: Map<String, String> = emptyMap(),
    @Transient val instance: BlueprintSpecInstance? = null,
)

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

        @OptIn(InternalSerializationApi::class)
        override val descriptor: SerialDescriptor = buildSerialDescriptor(
            serialName = "gg.kuken.feature.blueprint.model.BlueprintSpecImage",
            kind = PolymorphicKind.SEALED
        )

        override fun deserialize(decoder: Decoder): BlueprintSpecImage = try {
            Identifier(decoder.decodeString())
        } catch (_: SerializationException) {
            decoder.decodeStructure(descriptor) {
                try {
                    val imageList = decodeSerializableElement(
                        descriptor = descriptor,
                        index = 0,
                        deserializer = ListSerializer(String.serializer()),
                    )

                    MultipleIdentifier(imageList)
                } catch (_: SerializationException) {
                    val imageList = decodeSerializableElement(
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
            value: BlueprintSpecImage
        ) = when(value) {
            is Identifier -> encoder.encodeString(value.id)
            is MultipleIdentifier -> encoder.encodeStructure(descriptor) {
                value.images.forEachIndexed { index, string ->
                    encodeStringElement(descriptor, index, string)
                }
            }
            is MultipleRef -> encoder.encodeStructure(descriptor) {
                value.images.forEachIndexed { index, ref ->
                    encodeSerializableElement(descriptor, index, Ref.serializer(), ref)
                }
            }
            is Ref -> encoder.encodeSerializableValue(Ref.serializer(), value)
        }
    }
}

@Serializable
data class BlueprintSpecInstance(
    val name: String,
)
