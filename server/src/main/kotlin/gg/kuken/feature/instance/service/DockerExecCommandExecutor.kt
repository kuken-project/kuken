package gg.kuken.feature.instance.service

import gg.kuken.feature.blueprint.BlueprintSpecProvider
import gg.kuken.feature.blueprint.processor.BlueprintProcessor
import gg.kuken.feature.blueprint.processor.InstanceSettingsCommandExecutor
import gg.kuken.feature.blueprint.processor.NoopBlueprintResourceReader
import gg.kuken.feature.blueprint.service.BlueprintService
import gg.kuken.feature.instance.model.Instance
import me.devnatan.dockerkt.DockerClient
import me.devnatan.dockerkt.resource.exec.create
import me.devnatan.dockerkt.resource.exec.start
import org.apache.logging.log4j.LogManager

/**
 * Executes commands in instances using Docker exec.
 *
 * Processes blueprint SSH command templates and executes them
 * within the Docker container using docker exec.
 */
class DockerExecCommandExecutor(
    private val dockerClient: DockerClient,
    private val blueprintService: BlueprintService,
    private val blueprintSpecProvider: BlueprintSpecProvider,
    private val blueprintProcessor: BlueprintProcessor,
) : InstanceCommandExecutor {
    private val logger = LogManager.getLogger(DockerExecCommandExecutor::class.java)

    override suspend fun executeCommand(
        instance: Instance,
        commandToRun: String,
    ): Int? {
        val blueprint = blueprintService.getBlueprint(instance.blueprintId)
        val specText = blueprintSpecProvider.provide(blueprint.origin)
        val processor = blueprintProcessor.process(specText, listOf(NoopBlueprintResourceReader))
        val executor =
            when (val value = processor.instanceSettings?.commandExecutor) {
                null -> error("No command executor was specified")
                !is InstanceSettingsCommandExecutor.SSH -> error("Only SSH command execution are supported for now")
                else -> value
            }

        val template = executor.template.replace("\${refs.instance.command}", commandToRun)
        logger.debug("Sending command to {}: {}", instance.id, template)

        val execId =
            dockerClient.exec.create(instance.containerId!!) {
                tty = true
                attachStdin = false
                attachStdout = false
                attachStderr = false
                user = "1000"
                command = template.split(" ")
            }

        dockerClient.exec.start(execId) {
            detach = true
        }

        return dockerClient.exec.inspect(execId).exitCode
    }
}
