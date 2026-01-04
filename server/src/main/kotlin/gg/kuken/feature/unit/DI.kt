package gg.kuken.feature.unit

import gg.kuken.feature.unit.data.entity.UnitRepositoryImpl
import gg.kuken.feature.unit.data.repository.UnitRepository
import gg.kuken.feature.unit.http.mapper.UnitInstanceMapper
import gg.kuken.feature.unit.http.mapper.UnitMapper
import org.koin.dsl.module

val UnitDI =
    module {
        single<UnitRepository>(createdAtStart = true) {
            UnitRepositoryImpl(database = get())
        }

        factory {
            UnitService(
                kukenConfig = get(),
                unitRepository = get(),
                identityGeneratorService = get(),
                instanceService = get(),
            )
        }

        factory {
            UnitInstanceMapper(
                blueprintService = get(),
            )
        }

        factory {
            UnitMapper(
                unitInstanceMapper = get(),
                instanceService = get(),
            )
        }
    }
