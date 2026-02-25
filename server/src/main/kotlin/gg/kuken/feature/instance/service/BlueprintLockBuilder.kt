package gg.kuken.feature.instance.service

import gg.kuken.feature.blueprint.BlueprintPropertyResolver
import gg.kuken.feature.blueprint.processor.BlueprintResolutionContext
import gg.kuken.feature.blueprint.processor.BlueprintResolutionContextInputs
import gg.kuken.feature.blueprint.processor.InstanceSettingsCommandExecutor
import gg.kuken.feature.blueprint.processor.ResolvedBlueprint
import gg.kuken.feature.instance.model.BlueprintLock
import gg.kuken.feature.instance.model.LockCommandExecutor
import gg.kuken.feature.instance.model.LockDockerConfig
import gg.kuken.feature.instance.model.LockHooks
import gg.kuken.feature.instance.model.LockInstanceSettings
import gg.kuken.feature.instance.model.LockMetadata
import gg.kuken.feature.instance.model.LockResource
import kotlin.uuid.Uuid

class BlueprintLockBuilder(
    private val blueprintPropertyResolver: BlueprintPropertyResolver,
) {
    fun buildLock(
        blueprintId: Uuid,
        resolvedBlueprint: ResolvedBlueprint,
        context: BlueprintResolutionContext,
        inputs: BlueprintResolutionContextInputs,
    ): BlueprintLock {
        val image = requireNotNull(
            blueprintPropertyResolver.resolve(
                property = resolvedBlueprint.build.docker.image,
                blueprint = resolvedBlueprint,
                context = context,
            ),
        ) { "Docker image cannot be null" }

        val environmentVariables = resolvedBlueprint.build.environmentVariables
            .mapNotNull { env ->
                val value = blueprintPropertyResolver.resolve(env.value, resolvedBlueprint, context) ?: return@mapNotNull null
                env.name to value
            }
            .toMap()

        val instanceSettings = resolvedBlueprint.instanceSettings?.let { settings ->
            val startup = blueprintPropertyResolver.resolve(settings.startup, resolvedBlueprint, context)
            val commandExecutor = settings.commandExecutor?.let { executor ->
                when (executor) {
                    is InstanceSettingsCommandExecutor.Rcon -> {
                        val port = requireNotNull(
                            blueprintPropertyResolver.resolve(executor.port, resolvedBlueprint, context),
                        ) { "RCON port cannot be null" }.toInt()
                        val password = requireNotNull(
                            blueprintPropertyResolver.resolve(executor.password, resolvedBlueprint, context),
                        ) { "RCON password cannot be null" }
                        LockCommandExecutor.Rcon(
                            port = port,
                            password = password,
                            template = executor.template,
                        )
                    }
                    is InstanceSettingsCommandExecutor.SSH -> {
                        LockCommandExecutor.SSH(template = executor.template)
                    }
                }
            }
            LockInstanceSettings(startup = startup, commandExecutor = commandExecutor)
        }

        val resources = resolvedBlueprint.resources.map { resource ->
            LockResource(name = resource.name, source = resource.source)
        }

        val hooks = LockHooks(
            onInstall = resolvedBlueprint.hooks.onInstall?.let { hook ->
                LockResource(name = hook.name, source = hook.source)
            },
        )

        val metadata = LockMetadata(
            name = resolvedBlueprint.metadata.name,
            version = resolvedBlueprint.metadata.version,
            url = resolvedBlueprint.metadata.url,
            author = resolvedBlueprint.metadata.author,
            iconPath = resolvedBlueprint.metadata.assets.icon,
        )

        return BlueprintLock(
            blueprintId = blueprintId,
            blueprintVersion = resolvedBlueprint.metadata.version,
            metadata = metadata,
            docker = LockDockerConfig(image = image),
            environmentVariables = environmentVariables,
            inputs = inputs,
            instanceSettings = instanceSettings,
            resources = resources,
            hooks = hooks,
        )
    }
}
