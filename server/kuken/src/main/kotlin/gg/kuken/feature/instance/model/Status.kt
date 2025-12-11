package gg.kuken.feature.instance.model

import kotlinx.serialization.Serializable

@Serializable
sealed class InstanceStatus(
    val value: String,
    val isInitialStatus: Boolean = false,
    val isRuntimeStatus: Boolean = false,
) {
    data object Created : InstanceStatus(value = "created")

    data object NetworkAssignmentFailed : InstanceStatus(value = "network-assignment-failed")

    data object Unavailable : InstanceStatus(value = "unavailable")

    data object Unknown : InstanceStatus(value = "unknown")

    data object ImagePullInProgress : InstanceStatus("image-pull", isInitialStatus = true)

    data object ImagePullNeeded : InstanceStatus(value = "image-pull-needed", isInitialStatus = true)

    data object ImagePullFailed : InstanceStatus(value = "image-pull-failed", isInitialStatus = true)

    data object ImagePullCompleted : InstanceStatus(value = "image-pull-completed", isInitialStatus = true)

    data object Dead : InstanceStatus(value = "dead", isRuntimeStatus = true)

    data object Paused : InstanceStatus(value = "paused", isRuntimeStatus = true)

    data object Exited : InstanceStatus(value = "exited", isRuntimeStatus = true)

    data object Running : InstanceStatus(value = "running", isRuntimeStatus = true)

    data object Stopped : InstanceStatus(value = "stopped", isRuntimeStatus = true)

    data object Starting : InstanceStatus(value = "starting", isRuntimeStatus = true)

    data object Removing : InstanceStatus(value = "removing", isRuntimeStatus = true)

    data object Stopping : InstanceStatus(value = "stopping", isRuntimeStatus = true)

    data object Restarting : InstanceStatus(value = "restarting", isRuntimeStatus = true)
}

@Serializable
@Suppress("detekt.MagicNumber")
sealed class InstanceUpdateCode(
    val name: String,
    val code: Int,
) {
    object Start : InstanceUpdateCode(name = "start", code = 1)

    object Stop : InstanceUpdateCode(name = "stop", code = 2)

    object Restart : InstanceUpdateCode(name = "restart", code = 3)

    object Kill : InstanceUpdateCode(name = "kill", code = 4)

    companion object {
        private val mappings: Map<Int, InstanceUpdateCode> by lazy {
            listOf(Start, Stop, Restart, Kill).associateBy(InstanceUpdateCode::code)
        }

        @JvmStatic
        fun getByCode(code: Int): InstanceUpdateCode? = mappings[code]
    }

    override fun toString(): String = "$name ($code)"
}
