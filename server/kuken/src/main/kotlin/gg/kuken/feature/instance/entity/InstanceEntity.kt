@file:OptIn(ExperimentalUuidApi::class)

package gg.kuken.feature.instance.entity

import gg.kuken.feature.instance.model.Instance
import gg.kuken.feature.instance.repository.InstanceRepository
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

object InstanceTable : UUIDTable("instances") {
    val imageUpdatePolicy = varchar("image_update_policy", length = 64)
    val containerId = varchar("cid", length = 255).nullable()
    val blueprintId = uinteger("bid")
    val host = varchar("host", length = 255).nullable()
    val port = short("port").nullable()
    val status = varchar("status", length = 255)
    val createdAt = timestamp("created_at")
}

class InstanceEntity(
    id: EntityID<UUID>,
) : UUIDEntity(id) {
    companion object : UUIDEntityClass<InstanceEntity>(InstanceTable)

    var updatePolicy by InstanceTable.imageUpdatePolicy
    var containerId by InstanceTable.containerId
    var blueprintId by InstanceTable.blueprintId
    var host by InstanceTable.host
    var port by InstanceTable.port
    var status by InstanceTable.status
    var createdAt by InstanceTable.createdAt
}

class InstanceRepositoryImpl(
    private val database: Database,
) : InstanceRepository {
    init {
        transaction(db = database) {
            SchemaUtils.create(InstanceTable)
        }
    }

    override suspend fun findById(id: Uuid): InstanceEntity? =
        newSuspendedTransaction(db = database) {
            InstanceEntity.findById(id.toJavaUuid())
        }

    override suspend fun create(instance: Instance) {
        newSuspendedTransaction(db = database) {
            InstanceEntity.new(instance.id.toJavaUuid()) {
                updatePolicy = instance.updatePolicy.id
                containerId = instance.containerId
                blueprintId = instance.blueprintId
                host = instance.connection?.host
                port = instance.connection?.port
                status = instance.status.value
            }
        }
    }

    override suspend fun delete(id: Uuid) {
        newSuspendedTransaction(db = database) {
            InstanceEntity.findById(id.toJavaUuid())?.delete()
        }
    }

    override suspend fun update(
        id: Uuid,
        update: InstanceEntity.() -> Unit,
    ): InstanceEntity? =
        newSuspendedTransaction(db = database) {
            InstanceEntity.findById(id.toJavaUuid())?.apply(update)
        }
}
