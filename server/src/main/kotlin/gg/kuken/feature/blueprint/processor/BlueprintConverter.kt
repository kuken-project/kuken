package gg.kuken.feature.blueprint.processor

import org.pkl.core.ModuleSource
import org.pkl.core.PModule
import org.pkl.core.PNull
import org.pkl.core.PObject
import org.pkl.core.resource.ResourceReader
import java.lang.AutoCloseable
import java.net.URI
import java.util.Optional

class BlueprintConverter : AutoCloseable {
    private val evaluator =
        org.pkl.core.EvaluatorBuilder
            .preconfigured()
            .setAllowedResources(listOf(Regex("^kuken:.+$").toPattern()))
            .addResourceReader(
                object : ResourceReader {
                    override fun getUriScheme(): String = "kuken"

                    override fun read(uri: URI?): Optional<in Any> = Optional.of("<unresolved:$uri>")

                    override fun hasHierarchicalUris(): Boolean = false

                    override fun isGlobbable(): Boolean = false
                },
            ).build()
    var objectCache: ObjectCache = ObjectCache()

    fun convert(source: ModuleSource): ResolvedBlueprint {
        val module = evaluator.evaluate(source)
        return convertModule(module)
    }

    @Suppress("UNCHECKED_CAST")
    private fun convertModule(module: PModule): ResolvedBlueprint {
        val inputsObj = module.getProperty("inputs") as List<PObject>
        val inputs = collectInputs(inputsObj)

        val buildObj = module.getProperty("build") as PObject
        val envVarsObj = (buildObj.getProperty("environmentVariables") as List<PObject>)
        val envVars = collectEnvVars(envVarsObj)

        objectCache =
            ObjectCache(
                inputs =
                    inputs.associate {
                        when (val name = it.name) {
                            is Resolvable.Literal -> name.value to it
                            else -> "" to it
                        }
                    },
                envVars = envVars.associateBy { it.name },
            )

        val metadata =
            BlueprintMetadata(
                name = module.getProperty("name") as String,
                version = module.getProperty("version") as String,
                url = module.getProperty("url") as String,
            )

        val assets = module.getPropertyOrNull("assets")?.takeUnless { it is PNull }?.let { convertAssets(it) }
        var resources =
            module
                .getPropertyOrNull("resources")
                ?.takeUnless { it is PNull }
                ?.let {
                    val list = it as List<PObject>
                    list.map { obj -> AppResource(name = "", source = obj.getProperty("source") as String) }
                }.orEmpty()

        val hooks =
            module
                .getPropertyOrNull("hooks")
                ?.takeUnless { it is PNull }
                ?.let {
                    val obj = it as PObject

                    var installResource =
                        obj
                            .getProperty("onInstall")
                            ?.takeUnless { it is PNull }
                            ?.let { (it as PObject).get("source") as String }
                            .let { source -> resources.firstOrNull { v -> v.source == source } }

                    if (installResource != null) {
                        installResource = AppResource(name = "install", source = installResource.source)
                        resources = resources.filterNot { v -> v.source == installResource.source } +
                            listOf(installResource)
                    }

                    AppHooks(
                        onInstall = installResource,
                    )
                }

        val build = convertBuildConfig(buildObj, envVars)

        val instanceSettings =
            module
                .getProperty("instance")
                ?.takeUnless { instance -> instance is PNull }
                ?.let { instance -> instance as PObject }
                ?.let { instance ->
                    val startup = UniversalPklParser.parseValue<String>(instance.getProperty("startup"))
                    val commandExecutor =
                        instance.getProperty("command").let { it as PObject }.let {
                            when (val type = it.getProperty("type")) {
                                "rcon" -> {
                                    val port = UniversalPklParser.parseValue<Int>(it.getProperty("port"))
                                    val password = UniversalPklParser.parseValue<String>(it.getProperty("password"))
                                    val template = it.getProperty("template") as String

                                    InstanceSettingsCommandExecutor.Rcon(port, password, template)
                                }

                                "ssh" -> {
                                    val template = it.getProperty("template") as String

                                    InstanceSettingsCommandExecutor.SSH(template)
                                }

                                else -> {
                                    error("Unknown instance command executor type: $type")
                                }
                            }
                        }
                    InstanceSettings(
                        startup = startup,
                        commandExecutor = commandExecutor,
                    )
                }

        return ResolvedBlueprint(
            metadata = metadata,
            assets = assets,
            inputs = inputs,
            build = build,
            instanceSettings = instanceSettings,
            resources = resources,
            hooks = hooks ?: AppHooks(),
        )
    }

    private fun collectInputs(inputsObj: List<PObject>): List<UserInput> = inputsObj.map(::convertUserInput)

    private fun convertUserInput(inputObj: PObject): UserInput {
        val type = inputObj.getProperty("type") as String
        val name = inputObj.getProperty("name") as String
        val label = inputObj.getProperty("label") as String

        return when (type) {
            "text" -> {
                TextInput(name, label)
            }

            "password" -> {
                PasswordInput(name, label)
            }

            "port" -> {
                val default: Resolvable<Int> =
                    UniversalPklParser.parseValue(inputObj.getProperty("default"))
                PortInput(name, label, default)
            }

            "checkbox" -> {
                val default: Resolvable<Boolean> =
                    UniversalPklParser.parseValue(inputObj.getProperty("default"))
                CheckboxInput(name, label, default)
            }

            "select" -> {
                val items = inputObj.getProperty("items") as List<String>
                SelectInput(name, label, items)
            }

            "datasize" -> {
                DataSizeInput(name, label)
            }

            else -> {
                throw IllegalArgumentException("Unknown input type: $type")
            }
        }
    }

    private fun collectEnvVars(envVarsObj: List<PObject>): List<EnvironmentVariable> {
        val envVars = mutableListOf<EnvironmentVariable>()

        for (element in envVarsObj) {
            val name = element.getProperty("name") as String
            val valueObj: Resolvable<String> = UniversalPklParser.parseValue(element.getProperty("value"))

            envVars.add(EnvironmentVariable(name, valueObj))
        }

        return envVars
    }

    private fun convertAssets(assetsObj: Any): AppAssets {
        val assetsMap = assetsObj as PObject
        val icon = assetsMap.getProperty("icon") as String
        return AppAssets(icon = icon)
    }

    private fun convertBuildConfig(
        buildObj: PObject,
        environmentVariables: List<EnvironmentVariable>,
    ): BuildConfig {
        val dockerObj = buildObj.getProperty("docker") as PObject
        val docker = convertDockerConfig(dockerObj)

        return BuildConfig(docker = docker, environmentVariables = environmentVariables)
    }

    private fun convertDockerConfig(dockerObj: PObject): DockerConfig {
        val image: Resolvable<String> = UniversalPklParser.parseValue(dockerObj.getProperty("image"))
        return DockerConfig(image = image)
    }

    override fun close() {
        evaluator.close()
    }
}
