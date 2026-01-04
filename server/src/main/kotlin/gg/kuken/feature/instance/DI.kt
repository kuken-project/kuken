package gg.kuken.feature.instance

import gg.kuken.core.docker.DockerNetworkService
import gg.kuken.feature.instance.data.entity.InstanceRepositoryImpl
import gg.kuken.feature.instance.data.repository.InstanceRepository
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
                eventDispatcher = get(),
            )
        }

        factory<InstanceFileService> {
            InstanceFileService(
                instanceService = get(),
                dockerClient = get(),
            )
        }

        single<InstanceEventsRedirector>(createdAtStart = true) {
            InstanceEventsRedirector(
                webSocketManager = get(),
                eventDispatcher = get(),
            )
        }
    }
