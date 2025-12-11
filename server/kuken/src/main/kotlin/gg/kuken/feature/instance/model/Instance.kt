@file:OptIn(ExperimentalUuidApi::class)

package gg.kuken.feature.instance.model

import gg.kuken.feature.instance.InstanceUnreachableRuntimeException
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
class Instance(
    val id: Uuid,
    val status: InstanceStatus,
    val containerId: String?,
    val updatePolicy: ImageUpdatePolicy,
    val connection: HostPort?,
    val runtime: InstanceRuntime?,
    val blueprintId: Uuid,
    val createdAt: Instant,
    val nodeId: String,
)

val Instance.containerIdOrThrow: String
    get() = containerId ?: throw InstanceUnreachableRuntimeException()

val Instance.runtimeOrThrow: InstanceRuntime
    get() = runtime ?: throw InstanceUnreachableRuntimeException()
