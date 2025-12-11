package gg.kuken.feature.blueprint.entity

import gg.kuken.feature.blueprint.repository.BlueprintRepository
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

object BlueprintTable : UUIDTable("blueprints") {
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val content = blob("content")
}

class BlueprintEntity(
    id: EntityID<UUID>,
) : UUIDEntity(id) {
    companion object : UUIDEntityClass<BlueprintEntity>(BlueprintTable)

    var createdAt: Instant by BlueprintTable.createdAt
    var updatedAt: Instant by BlueprintTable.updatedAt
    var content: ExposedBlob by BlueprintTable.content
}

class BlueprintRepositoryImpl(
    private val database: Database,
) : BlueprintRepository {
    init {
        transaction(db = database) {
            SchemaUtils.createMissingTablesAndColumns(BlueprintTable)
        }
    }

    override suspend fun findAll(): List<BlueprintEntity> =
        newSuspendedTransaction(db = database) {
            BlueprintEntity.all().notForUpdate().toList()
        }

    override suspend fun find(id: Uuid): BlueprintEntity? =
        newSuspendedTransaction(db = database) {
            BlueprintEntity.findById(id.toJavaUuid())
        }

    override suspend fun create(
        id: Uuid,
        spec: ByteArray,
        createdAt: Instant,
    ) {
        newSuspendedTransaction(db = database) {
            BlueprintEntity.new(id.toJavaUuid()) {
                content = ExposedBlob(spec)
                this.createdAt = createdAt
                this.updatedAt = createdAt
            }
        }
    }

    override suspend fun delete(id: Uuid) {
        newSuspendedTransaction(db = database) {
            BlueprintEntity.findById(id.toJavaUuid())?.delete()
        }
    }
}
