package gg.kuken.feature.instance

import gg.kuken.feature.instance.entity.InstanceRepositoryImpl
import gg.kuken.feature.instance.repository.InstanceRepository
import gg.kuken.feature.instance.service.InstanceFileService
import org.koin.dsl.module

val InstancesDI =
    module {
        single<InstanceRepository>(createdAtStart = true) {
            InstanceRepositoryImpl(database = get())
        }

        factory<InstanceService> {
            DockerInstanceService(
                dockerClient = get(),
                instanceRepository = get(),
                blueprintService = get(),
                identityGeneratorService = get(),
                kukenConfig = get(),
                dockerNetworkService = DockerNetworkService(dockerClient = get()),
            )
        }

        factory<InstanceFileService> {
            InstanceFileService(
                instanceService = get(),
                dockerClient = get(),
            )
        }
    }
