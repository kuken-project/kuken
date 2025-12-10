package gg.kuken.feature.account

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class IdentityGeneratorService {
    fun generate(): Uuid = Uuid.Companion.random()
}
