package gg.kuken.feature.blueprint.model

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigParseOptions
import com.typesafe.config.ConfigSyntax
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BlueprintSpecImageSerializerTest {

    private val hocon = Hocon {
        useConfigNamingConvention = true
    }

    @Serializable
    private data class Data(val image: BlueprintSpecImage)

    private fun parse(input: String): BlueprintSpecImage {
        val config = ConfigFactory.parseString(
            input,
            ConfigParseOptions.defaults().setSyntax(ConfigSyntax.CONF)
        )

        return hocon.decodeFromConfig<Data>(config).image
    }

    @Test
    fun `deserialize identifier`() {
        val spec = parse("image = \"busybox:latest\"")

        assertTrue(spec is BlueprintSpecImage.Identifier)
        assertEquals(expected = "busybox:latest", actual = spec.id)
    }

    @Test
    fun `deserialize multiple identifier`() {
        val spec = parse("""
            image = [
                "busybox:latest",
                "hello:world"
            ]
        """.trimIndent())

        assertTrue(spec is BlueprintSpecImage.MultipleIdentifier)
        assertEquals(listOf(
            "busybox:latest",
            "hello:world"
        ), spec.images)
    }

    @Test
    fun `deserialize multiple ref`() {
        val spec = parse("""
            image = [
                {
                    label = "Java 17"
                    tag = "openjdk:17"
                },
                {
                    label = "Java 21"
                    tag = "openjdk:21"
                }
            ]
        """.trimIndent())

        assertTrue(spec is BlueprintSpecImage.MultipleRef)
        assertEquals(listOf(
            BlueprintSpecImage.Ref(label = "Java 17", tag = "openjdk:17"),
            BlueprintSpecImage.Ref(label = "Java 21", tag = "openjdk:21"),
        ), spec.images)
    }
}