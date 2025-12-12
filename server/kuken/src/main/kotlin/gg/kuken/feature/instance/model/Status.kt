package gg.kuken.feature.instance.model

import kotlinx.serialization.Serializable

@Serializable
enum class InstanceStatus(
    val label: String,
) {
    Created(label = "created"),
    NetworkAssignmentFailed(label = "network-assignment-failed"),
    Unavailable(label = "unavailable"),
    Unknown(label = "unknown"),
    ImagePullInProgress(label = "image-pull"),
    ImagePullNeeded(label = "image-pull-needed"),
    ImagePullFailed(label = "image-pull-failed"),
    ImagePullCompleted(label = "image-pull-completed"),
    Dead(label = "dead"),
    Paused(label = "paused"),
    Exited(label = "exited"),
    Running(label = "running"),
    Stopped(label = "stopped"),
    Starting(label = "starting"),
    Removing(label = "removing"),
    Stopping(label = "stopping"),
    Restarting(label = "restarting"),
    ;

    val isInitialStatus: Boolean
        get() =
            when (this) {
                ImagePullInProgress, ImagePullNeeded, ImagePullFailed, ImagePullCompleted -> true
                else -> false
            }

    val isRuntimeStatus: Boolean
        get() =
            when (this) {
                Dead, Paused, Exited, Running, Stopped, Starting, Removing, Stopping, Restarting -> true
                else -> false
            }

    companion object {
        fun getByLabel(label: String) = entries.firstOrNull { it.label == label } ?: Unknown
    }
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
