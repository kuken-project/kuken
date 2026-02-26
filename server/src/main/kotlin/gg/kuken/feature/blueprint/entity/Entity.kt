package gg.kuken.feature.blueprint.entity

import gg.kuken.feature.blueprint.model.BlueprintHeader
import gg.kuken.feature.blueprint.model.BlueprintStatus
import gg.kuken.feature.blueprint.repository.BlueprintRepository
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

object BlueprintTable : UUIDTable("blueprints") {
    val name = varchar("name", 255)
    val version = varchar("version", 255)
    val url = varchar("url", 255)
    val author = varchar("author", 255).default("")
    val icon = blob("icon").nullable()
    val origin = varchar("origin", 255)
    val createdAt = timestamp("created_at")
    val status = enumeration<BlueprintStatus>("status")
}

class BlueprintEntity(
    id: EntityID<UUID>,
) : UUIDEntity(id) {
    companion object : UUIDEntityClass<BlueprintEntity>(BlueprintTable)

    var name: String by BlueprintTable.name
    var version: String by BlueprintTable.version
    var url: String by BlueprintTable.url
    var author: String by BlueprintTable.author
    var icon by BlueprintTable.icon
    var origin: String by BlueprintTable.origin
    var createdAt: Instant by BlueprintTable.createdAt
    var status: BlueprintStatus by BlueprintTable.status
}

class BlueprintRepositoryImpl(
    private val database: Database,
) : BlueprintRepository {
    init {
        transaction(db = database) {
            @Suppress("DEPRECATION")
            SchemaUtils.createMissingTablesAndColumns(BlueprintTable)
        }
    }

    override suspend fun findAll(): List<BlueprintEntity> =
        suspendTransaction(db = database) {
            BlueprintEntity.all().notForUpdate().toList()
        }

    override suspend fun find(id: Uuid): BlueprintEntity? =
        suspendTransaction(db = database) {
            BlueprintEntity.findById(id.toJavaUuid())
        }

    override suspend fun create(
        id: Uuid,
        origin: String,
        createdAt: Instant,
        status: BlueprintStatus,
        header: BlueprintHeader,
    ): BlueprintEntity =
        suspendTransaction(db = database) {
            BlueprintEntity.new(id.toJavaUuid()) {
                this.name = header.name
                this.url = header.url
                this.version = header.version
                this.origin = origin
                this.createdAt = createdAt
                this.status = status
            }
        }

    override suspend fun delete(id: Uuid) {
        suspendTransaction(db = database) {
            BlueprintEntity.findById(id.toJavaUuid())?.delete()
        }
    }
}
