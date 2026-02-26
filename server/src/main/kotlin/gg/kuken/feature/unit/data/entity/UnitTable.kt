package gg.kuken.feature.unit.data.entity

import gg.kuken.feature.instance.data.entity.InstanceTable
import gg.kuken.feature.unit.data.repository.UnitRepository
import gg.kuken.feature.unit.model.KukenUnit
import org.jetbrains.exposed.v1.core.ReferenceOption
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

object UnitTable : UUIDTable("units") {
    val externalId = varchar("ext_id", length = 255).nullable()
    val name = varchar("name", length = 255)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()
    val instanceId = uuid("instance_id").references(InstanceTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val status = varchar("status", length = 255)
}

class UnitEntity(
    id: EntityID<UUID>,
) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UnitEntity>(UnitTable)

    var externalId by UnitTable.externalId
    var name by UnitTable.name
    var createdAt by UnitTable.createdAt
    var updatedAt by UnitTable.updatedAt
    var deletedAt by UnitTable.deletedAt
    var instanceId by UnitTable.instanceId
    var status by UnitTable.status
}

class UnitRepositoryImpl(
    private val database: Database,
) : UnitRepository {
    init {
        transaction(db = database) {
            @Suppress("DEPRECATION")
            SchemaUtils.createMissingTablesAndColumns(UnitTable)
        }
    }

    override suspend fun listUnits(): List<UnitEntity> =
        suspendTransaction(db = database, readOnly = true) {
            UnitEntity.all().notForUpdate().toList()
        }

    override suspend fun findById(id: Uuid): UnitEntity? =
        suspendTransaction(db = database, readOnly = true) {
            UnitEntity.findById(id.toJavaUuid())
        }

    override suspend fun createUnit(unit: KukenUnit) {
        suspendTransaction(db = database) {
            UnitEntity.new(unit.id.toJavaUuid()) {
                this.name = unit.name
                this.externalId = unit.externalId
                this.instanceId = unit.instanceId?.toJavaUuid()
                this.createdAt = unit.createdAt
                this.updatedAt = unit.updatedAt
                this.deletedAt = unit.deletedAt
                this.status = unit.status.value
            }
        }
    }
}
