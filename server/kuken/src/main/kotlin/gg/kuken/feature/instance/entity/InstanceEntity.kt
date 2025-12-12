package gg.kuken.feature.instance.entity

import gg.kuken.feature.instance.model.Instance
import gg.kuken.feature.instance.repository.InstanceRepository
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

object InstanceTable : UUIDTable("instances") {
    val imageUpdatePolicy = varchar("image_update_policy", length = 64)
    val containerId = varchar("cid", length = 255).nullable()
    val blueprintId = uuid("bid")
    val host = varchar("host", length = 255).nullable()
    val port = ushort("port").nullable()
    val status = varchar("status", length = 255)
    val createdAt = timestamp("created_at")
    val nodeId = varchar("node", length = 255)
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
    var nodeId by InstanceTable.nodeId
}

class InstanceRepositoryImpl(
    private val database: Database,
) : InstanceRepository {
    init {
        transaction(db = database) {
            SchemaUtils.createMissingTablesAndColumns(InstanceTable)
        }
    }

    override suspend fun findById(id: Uuid): InstanceEntity? =
        suspendTransaction(db = database) {
            InstanceEntity.findById(id.toJavaUuid())
        }

    override suspend fun create(instance: Instance) {
        suspendTransaction(db = database) {
            InstanceEntity.new(instance.id.toJavaUuid()) {
                updatePolicy = instance.updatePolicy.id
                containerId = instance.containerId
                blueprintId = instance.blueprintId.toJavaUuid()
                host = instance.address?.host
                port = instance.address?.port
                status = instance.status.label
                nodeId = instance.nodeId
                createdAt = instance.createdAt
            }
        }
    }

    override suspend fun delete(id: Uuid): InstanceEntity? =
        suspendTransaction(db = database) {
            InstanceEntity.findById(id.toJavaUuid())?.also(InstanceEntity::delete)
        }

    override suspend fun update(
        id: Uuid,
        update: InstanceEntity.() -> Unit,
    ): InstanceEntity? =
        suspendTransaction(db = database) {
            InstanceEntity.findById(id.toJavaUuid())?.apply(update)
        }
}
