package gg.kuken.feature.unit

import gg.kuken.KukenConfig
import gg.kuken.feature.account.IdentityGeneratorService
import gg.kuken.feature.instance.InstanceService
import gg.kuken.feature.instance.model.CreateInstanceOptions
import gg.kuken.feature.instance.model.InstanceStatus
import gg.kuken.feature.unit.entity.UnitEntity
import gg.kuken.feature.unit.model.KukenUnit
import gg.kuken.feature.unit.model.UnitCreateOptions
import gg.kuken.feature.unit.model.UnitStatus
import gg.kuken.feature.unit.repository.UnitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

internal class UnitService(
    private val kukenConfig: KukenConfig,
    private val unitRepository: UnitRepository,
    private val identityGeneratorService: IdentityGeneratorService,
    private val instanceService: InstanceService,
) : CoroutineScope by CoroutineScope(SupervisorJob()) {
    suspend fun getUnits(): List<KukenUnit> = unitRepository.listUnits().map(::KukenUnit)

    suspend fun getUnit(id: Uuid): KukenUnit = unitRepository.findById(id)?.let(::KukenUnit) ?: throw UnitNotFoundException()

    suspend fun createUnit(options: UnitCreateOptions): KukenUnit {
        val generatedId = identityGeneratorService.generate()
        val instance =
            instanceService.createInstance(
                blueprintId = options.blueprintId,
                options =
                    CreateInstanceOptions(
                        image = options.image,
                        host = options.network?.host,
                        port = options.network?.port,
                        env = options.options,
                    ),
            )
        val status: UnitStatus =
            when (instance.status) {
                InstanceStatus.ImagePullFailed -> UnitStatus.MissingInstance
                InstanceStatus.ImagePullInProgress -> UnitStatus.CreatingInstance
                InstanceStatus.ImagePullNeeded -> UnitStatus.CreatingInstance
                else -> UnitStatus.Created
            }
        val createdAt = Clock.System.now()
        val unit =
            KukenUnit(
                id = generatedId,
                externalId = options.externalId,
                nodeId = kukenConfig.node,
                name = options.name,
                createdAt = createdAt,
                updatedAt = createdAt,
                status = status,
                deletedAt = null,
                instanceId = instance.id,
            )

        unitRepository.createUnit(unit)
        return unit
    }

    private fun KukenUnit(entity: UnitEntity) =
        with(entity) {
            KukenUnit(
                id = id.value.toKotlinUuid(),
                externalId = externalId,
                instanceId = instanceId?.toKotlinUuid(),
                nodeId = nodeId,
                name = name,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt,
                status = UnitStatus.getByValue(status),
            )
        }
}
