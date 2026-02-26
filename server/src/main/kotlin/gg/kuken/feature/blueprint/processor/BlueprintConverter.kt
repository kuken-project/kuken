package gg.kuken.feature.blueprint.processor

import org.pkl.core.EvaluatorBuilder
import org.pkl.core.ModuleSource
import org.pkl.core.PModule
import org.pkl.core.PNull
import org.pkl.core.PObject
import org.pkl.core.resource.ResourceReader
import java.lang.AutoCloseable

class BlueprintConverter : AutoCloseable {
    companion object {
        private val evaluatorBuilder =
            EvaluatorBuilder
                .preconfigured()
                .setAllowedResources(listOf(Regex("^kuken:.+$").toPattern()))
    }

    var objectCache: ObjectCache = ObjectCache()

    fun eval(
        source: ModuleSource,
        readers: List<ResourceReader>,
    ): PModule {
        check(readers.isNotEmpty()) { "Resource readers cannot be null" }

        val evaluator =
            evaluatorBuilder
                .addResourceReaders(readers)
                .build()

        val schema = evaluator.evaluateSchema(source)

        return evaluator.evaluate(source)
    }

    fun convert(
        source: ModuleSource,
        readers: List<ResourceReader>,
    ): ResolvedBlueprint = convertModule(eval(source, readers))

    fun convertPartial(
        source: ModuleSource,
        readers: List<ResourceReader>,
    ): ResolveBlueprintInputDefinitions {
        val module = eval(source, readers)

        @Suppress("UNCHECKED_CAST")
        val inputs = collectInputs(module.getProperty("inputs") as List<PObject>)
        val startup =
            module
                .getProperty("instance")
                .let { instance -> instance as PObject }
                .let { instance -> UniversalPklParser.parseValue<String>(instance.getProperty("startup")) }

        return ResolveBlueprintInputDefinitions(inputs, startup)
    }

    @Suppress("UNCHECKED_CAST")
    private fun convertModule(module: PModule): ResolvedBlueprint {
        val inputsObj = module.getProperty("inputs") as List<PObject>
        val inputs = collectInputs(inputsObj)

        val buildObj = module.getProperty("build") as PObject
        val envVarsObj = (buildObj.getProperty("env") as Map<String, Any>)
        val envVars = collectEnvVars(envVarsObj)

        objectCache =
            ObjectCache(
                inputs = inputs.associateBy { it.name },
                envVars = envVars.associateBy { it.name },
            )

        val assets = module.getPropertyOrNull("assets").let { convertAssets(it as Any) }
        val metadata =
            BlueprintMetadata(
                name = module.getProperty("name") as String,
                version = module.getProperty("version") as String,
                url = module.getProperty("url") as String,
                author = module.getProperty("author") as String,
                assets = assets,
            )

        var resources = extractResources(module)

        val hooks =
            module
                .getPropertyOrNull("hooks")
                ?.takeUnless { it is PNull }
                ?.let {
                    val obj = it as PObject

                    var installResource =
                        obj
                            .getPropertyOrNull("onInstall")
                            ?.let { onInstall -> (onInstall as PObject).get("source") as String }
                            .let { onInstall -> resources.firstOrNull { v -> v.source == onInstall } }

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
                .getPropertyOrNull("instance")
                ?.let { instance -> instance as PObject }
                ?.let { instance ->
                    val startup = UniversalPklParser.parseValue<String>(instance.getProperty("startup"))
                    val commandExecutor =
                        instance.getPropertyOrNull("command")?.let { command -> command as PObject }?.let { command ->
                            when (val type = command.getProperty("type")) {
                                "rcon" -> {
                                    val port = UniversalPklParser.parseValue<Int>(command.getProperty("port"))
                                    val password = UniversalPklParser.parseValue<String>(command.getProperty("password"))
                                    val template = command.getProperty("template") as String

                                    InstanceSettingsCommandExecutor.Rcon(port, password, template)
                                }

                                "ssh" -> {
                                    val template = command.getProperty("template") as String

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
            inputs = inputs,
            build = build,
            instanceSettings = instanceSettings,
            resources = resources,
            hooks = hooks ?: AppHooks(),
        )
    }

    public fun extractResources(module: PModule): List<AppResource> =
        module
            .get("resources")
            ?.let { resources ->
                @Suppress("UNCHECKED_CAST")
                val list = resources as List<PObject>

                list.map { obj -> AppResource(name = "", source = obj.getProperty("source") as String) }
            }.orEmpty()

    private fun collectInputs(inputsObj: List<PObject>): List<UserInput> = inputsObj.map(::convertUserInput)

    private fun convertUserInput(inputObj: PObject): UserInput {
        val type = inputObj.getProperty("type") as String
        val name = inputObj.getProperty("name") as String
        val description = (inputObj.getPropertyOrNull("description") as? String).orEmpty()
        val label = inputObj.getProperty("label") as String

        return when (type) {
            "text" -> {
                TextInput(name, label, description)
            }

            "password" -> {
                PasswordInput(name, label, description)
            }

            "port" -> {
                val default = inputObj.getProperty("default") as? Int
                PortInput(name, label, description, default)
            }

            "checkbox" -> {
                val default = inputObj.getProperty("default") as Boolean
                CheckboxInput(name, label, description, default)
            }

            "select" -> {
                @Suppress("UNCHECKED_CAST")
                val items = inputObj.getProperty("items") as Map<String, String>
                SelectInput(name, label, description, items)
            }

            "datasize" -> {
                DataSizeInput(name, description, label)
            }

            else -> {
                throw IllegalArgumentException("Unknown input type: $type")
            }
        }
    }

    private fun collectEnvVars(envVarsObj: Map<String, Any>): List<EnvironmentVariable> {
        val envVars = mutableListOf<EnvironmentVariable>()

        for ((name, value) in envVarsObj) {
            val parsedValue: Resolvable<String> = UniversalPklParser.parseValue(value)

            envVars.add(EnvironmentVariable(name, parsedValue))
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
    }
}
