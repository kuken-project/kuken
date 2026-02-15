package gg.kuken.feature.instance.service

import gg.kuken.feature.instance.model.Instance

/**
 * Service for executing commands in instances.
 *
 * Provides an abstraction for running commands within instance containers,
 * supporting different execution strategies (e.g., SSH, direct exec).
 */
interface InstanceCommandExecutor {
    /**
     * Executes a command in an instance.
     *
     * @param instance The instance to execute the command in
     * @param commandToRun The command to execute
     * @return The exit code of the command, or null if unavailable
     */
    suspend fun executeCommand(
        instance: Instance,
        commandToRun: String,
    ): Int?
}
