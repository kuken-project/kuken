package gg.kuken.feature.unit

import gg.kuken.feature.unit.entity.UnitRepositoryImpl
import gg.kuken.feature.unit.repository.UnitRepository
import org.koin.dsl.module

val UnitDI =
    module {
        single<UnitRepository>(createdAtStart = true) {
            UnitRepositoryImpl(database = get())
        }

        single {
            UnitService(
                kukenConfig = get(),
                unitRepository = get(),
                identityGeneratorService = get(),
                instanceService = get(),
            )
        }
    }
