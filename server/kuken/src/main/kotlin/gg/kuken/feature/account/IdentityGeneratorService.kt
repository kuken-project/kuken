package gg.kuken.feature.account

import kotlin.uuid.Uuid

class IdentityGeneratorService {
    fun generate(): Uuid = Uuid.random()
}
