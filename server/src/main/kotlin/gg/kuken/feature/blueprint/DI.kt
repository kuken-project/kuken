package gg.kuken.feature.blueprint

import gg.kuken.feature.blueprint.entity.BlueprintRepositoryImpl
import gg.kuken.feature.blueprint.processor.BlueprintConverter
import gg.kuken.feature.blueprint.processor.BlueprintProcessor
import gg.kuken.feature.blueprint.repository.BlueprintRepository
import gg.kuken.feature.blueprint.service.BlueprintService
import org.koin.dsl.module

val BlueprintDI =
    module {
        single<BlueprintRepository>(createdAtStart = true) {
            BlueprintRepositoryImpl(database = get())
        }

        factory {
            BlueprintService(
                blueprintRepository = get(),
                blueprintSpecProvider = get(),
                identityGeneratorService = get(),
                blueprintConverter = get(),
                blueprintProcessor = get(),
            )
        }

        single<BlueprintSpecProvider> {
            CombinedBlueprintSpecProvider(
                listOf(
                    LocalBlueprintSpecProvider(),
                    RemoteBlueprintSpecProvider(),
                ),
            )
        }

        factory {
            BlueprintConverter()
        }

        factory {
            BlueprintProcessor(blueprintConverter = get())
        }
    }
