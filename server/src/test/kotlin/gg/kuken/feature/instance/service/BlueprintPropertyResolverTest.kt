package gg.kuken.feature.instance.service

import gg.kuken.feature.blueprint.BlueprintPropertyResolver
import gg.kuken.feature.blueprint.processor.AppAssets
import gg.kuken.feature.blueprint.processor.AppHooks
import gg.kuken.feature.blueprint.processor.BlueprintMetadata
import gg.kuken.feature.blueprint.processor.BlueprintResolutionContext
import gg.kuken.feature.blueprint.processor.BuildConfig
import gg.kuken.feature.blueprint.processor.CheckboxInput
import gg.kuken.feature.blueprint.processor.DockerConfig
import gg.kuken.feature.blueprint.processor.Resolvable
import gg.kuken.feature.blueprint.processor.ResolvedBlueprint
import gg.kuken.feature.blueprint.processor.ResolvedBlueprintRefs
import gg.kuken.feature.blueprint.processor.TextInput
import gg.kuken.feature.instance.model.HostPort
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class BlueprintPropertyResolverTest {
    private val resolver = BlueprintPropertyResolver()

    @Test
    fun `resolve literal value`() {
        val property = Resolvable.Literal("test-value")
        val context = createContext()
        val blueprint = createBlueprint()

        val result = resolver.resolve(property, blueprint, context)

        assertEquals("test-value", result)
    }

    @Test
    fun `resolve null literal returns null`() {
        val property = Resolvable.Literal("null")
        val context = createContext()
        val blueprint = createBlueprint()

        val result = resolver.resolve(property, blueprint, context)

        assertNull(result)
    }

    @Test
    fun `resolve Null returns null`() {
        val property = Resolvable.Null
        val context = createContext()
        val blueprint = createBlueprint()

        val result = resolver.resolve(property, blueprint, context)

        assertNull(result)
    }

    @Test
    fun `resolve environment variable reference`() {
        val property = Resolvable.EnvVarRef("TEST_VAR")
        val context = createContext(env = mapOf("TEST_VAR" to "test-value"))
        val blueprint = createBlueprint()

        val result = resolver.resolve(property, blueprint, context)

        assertEquals("test-value", result)
    }

    @Test
    fun `resolve missing environment variable returns empty string`() {
        val property = Resolvable.EnvVarRef("MISSING_VAR")
        val context = createContext()
        val blueprint = createBlueprint()

        val result = resolver.resolve(property, blueprint, context)

        assertEquals("", result)
    }

    @Test
    fun `resolve input reference with value`() {
        val property = Resolvable.InputRef("server-name")
        val context = createContext(inputs = mapOf("server-name" to "my-server"))
        val blueprint = createBlueprint()

        val result = resolver.resolve(property, blueprint, context)

        assertEquals("my-server", result)
    }

    @Test
    fun `resolve input reference with checkbox default`() {
        val property = Resolvable.InputRef("enable-feature")
        val context = createContext()
        val blueprint =
            createBlueprint(
                inputs =
                    listOf(
                        CheckboxInput(
                            name = "enable-feature",
                            label = "Enable Feature",
                            description = null,
                            default = true,
                        ),
                    ),
            )

        val result = resolver.resolve(property, blueprint, context)

        assertEquals("true", result)
    }

    @Test
    fun `resolve runtime reference for instance ID`() {
        val instanceId = Uuid.random()
        val property = Resolvable.RuntimeRef(ResolvedBlueprintRefs.INSTANCE_ID.key)
        val context = createContext(instanceId = instanceId)
        val blueprint = createBlueprint()

        val result = resolver.resolve(property, blueprint, context)

        assertEquals(instanceId.toString(), result)
    }

    @Test
    fun `resolve runtime reference for instance name`() {
        val property = Resolvable.RuntimeRef(ResolvedBlueprintRefs.INSTANCE_NAME.key)
        val context = createContext(instanceName = "test-instance")
        val blueprint = createBlueprint()

        val result = resolver.resolve(property, blueprint, context)

        assertEquals("test-instance", result)
    }

    @Test
    fun `resolve runtime reference for network host`() {
        val property = Resolvable.RuntimeRef(ResolvedBlueprintRefs.NETWORK_HOST.key)
        val context = createContext(address = HostPort("localhost", 8080u))
        val blueprint = createBlueprint()

        val result = resolver.resolve(property, blueprint, context)

        assertEquals("localhost", result)
    }

    @Test
    fun `resolve runtime reference for network port`() {
        val property = Resolvable.RuntimeRef(ResolvedBlueprintRefs.NETWORK_PORT.key)
        val context = createContext(address = HostPort("localhost", 8080u))
        val blueprint = createBlueprint()

        val result = resolver.resolve(property, blueprint, context)

        assertEquals("8080", result)
    }

    private fun createContext(
        instanceId: Uuid = Uuid.random(),
        instanceName: String = "test-instance",
        inputs: Map<String, String> = emptyMap(),
        env: Map<String, String> = emptyMap(),
        address: HostPort = HostPort("localhost", 8080u),
    ) = BlueprintResolutionContext(
        instanceId = instanceId,
        instanceName = instanceName,
        inputs = inputs,
        env = env,
        address = address,
    )

    private fun createBlueprint(
        inputs: List<gg.kuken.feature.blueprint.processor.UserInput> =
            listOf(
                TextInput(
                    name = "test",
                    label = "Test",
                    description = null,
                ),
            ),
    ) = ResolvedBlueprint(
        metadata =
            BlueprintMetadata(
                name = "test",
                version = "1.0.0",
                url = "https://test.com",
                author = "test",
                assets = AppAssets(icon = "icon.png"),
            ),
        resources = emptyList(),
        hooks = AppHooks(onInstall = null),
        inputs = inputs,
        build =
            BuildConfig(
                docker =
                    DockerConfig(
                        image = Resolvable.Literal("test:latest"),
                    ),
                environmentVariables = emptyList(),
            ),
        instanceSettings = null,
    )
}
