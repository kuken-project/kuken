package gg.kuken.feature.unit

import gg.kuken.KukenConfig
import gg.kuken.feature.account.IdentityGeneratorService
import gg.kuken.feature.instance.InstanceService
import gg.kuken.feature.instance.model.CreateInstanceOptions
import gg.kuken.feature.instance.model.HostPort
import gg.kuken.feature.instance.model.InstanceStatus
import gg.kuken.feature.unit.data.entity.UnitEntity
import gg.kuken.feature.unit.data.repository.UnitRepository
import gg.kuken.feature.unit.model.KukenUnit
import gg.kuken.feature.unit.model.UnitCreateOptions
import gg.kuken.feature.unit.model.UnitStatus
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
    suspend fun getUnits(): List<KukenUnit> = unitRepository.listUnits().map(::mapToKukenUnit)

    suspend fun getUnit(id: Uuid): KukenUnit = unitRepository.findById(id)?.let(::mapToKukenUnit) ?: throw UnitNotFoundException()

    suspend fun createUnit(payload: UnitCreateOptions): KukenUnit {
        val generatedId = identityGeneratorService.generate()
        val instance =
            instanceService.createInstance(
                options =
                    CreateInstanceOptions(
                        blueprint = payload.blueprintId,
                        inputs = payload.inputs,
                        env = payload.env,
                        address = HostPort(
                            host = payload.inputs["network.host"],
                            port = payload.inputs["network.port"]?.toUShortOrNull() ?: 25565u
                        ),
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
                externalId = payload.externalId,
                name = payload.name,
                createdAt = createdAt,
                updatedAt = createdAt,
                status = status,
                deletedAt = null,
                instanceId = instance.id,
            )

        unitRepository.createUnit(unit)
        return unit
    }

    private fun mapToKukenUnit(entity: UnitEntity) =
        with(entity) {
            KukenUnit(
                id = id.value.toKotlinUuid(),
                externalId = externalId,
                instanceId = instanceId?.toKotlinUuid(),
                name = name,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt,
                status = UnitStatus.getByValue(status),
            )
        }
}
