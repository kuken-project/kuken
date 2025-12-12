package gg.kuken.feature.blueprint

import gg.kuken.feature.blueprint.entity.BlueprintRepositoryImpl
import gg.kuken.feature.blueprint.parser.BlueprintParser
import gg.kuken.feature.blueprint.repository.BlueprintRepository
import org.koin.dsl.module

val BlueprintDI =
    module {
        single<BlueprintRepository>(createdAtStart = true) {
            BlueprintRepositoryImpl(database = get())
        }

        single {
            BlueprintService(
                blueprintRepository = get(),
                blueprintSpecProvider = get(),
                identityGeneratorService = get(),
            )
        }

        single<BlueprintSpecProvider> {
            CombinedBlueprintSpecProvider(
                listOf(
                    RemoteBlueprintSpecProvider(
                        parser = get(),
                    ),
                    LocalBlueprintSpecProvider(
                        parser = get(),
                    ),
                ),
            )
        }

        factory {
            BlueprintParser()
        }
    }
