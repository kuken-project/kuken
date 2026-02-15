package gg.kuken.feature.instance

import gg.kuken.core.docker.DockerContainerService
import gg.kuken.core.docker.DockerImageService
import gg.kuken.core.docker.DockerImageServiceImpl
import gg.kuken.core.docker.DockerNetworkService
import gg.kuken.feature.blueprint.BlueprintPropertyResolver
import gg.kuken.feature.instance.data.entity.InstanceRepositoryImpl
import gg.kuken.feature.instance.data.repository.InstanceRepository
import gg.kuken.feature.instance.service.DockerContainerServiceImpl
import gg.kuken.feature.instance.service.DockerExecCommandExecutor
import gg.kuken.feature.instance.service.InstanceCommandExecutor
import gg.kuken.feature.instance.service.InstanceInstaller
import gg.kuken.feature.instance.service.InstanceRuntimeBuilder
import org.koin.dsl.module

val InstancesDI =
    module {
        single<InstanceRepository>(createdAtStart = true) {
            InstanceRepositoryImpl(database = get())
        }

        // NEW: Blueprint property resolver
        factory<BlueprintPropertyResolver> {
            BlueprintPropertyResolver()
        }

        // NEW: Instance runtime builder
        factory<InstanceRuntimeBuilder> {
            InstanceRuntimeBuilder(
                dockerClient = get(),
            )
        }

        // NEW: Instance installer
        factory<InstanceInstaller> {
            InstanceInstaller(
                dockerClient = get(),
                kukenConfig = get(),
            )
        }

        // NEW: Docker container service
        factory<DockerContainerService> {
            DockerContainerServiceImpl(
                dockerClient = get(),
                instanceInstaller = get(),
            )
        }

        // NEW: Docker image service
        factory<DockerImageService> {
            DockerImageServiceImpl(
                dockerClient = get(),
            )
        }

        // NEW: Instance command executor
        factory<InstanceCommandExecutor> {
            DockerExecCommandExecutor(
                dockerClient = get(),
                blueprintService = get(),
                blueprintSpecProvider = get(),
                blueprintProcessor = get(),
            )
        }

        factory<InstanceService> {
            DockerInstanceService(
                instanceRepository = get(),
                blueprintService = get(),
                identityGeneratorService = get(),
                kukenConfig = get(),
                dockerNetworkService = DockerNetworkService(dockerClient = get()),
                blueprintSpecProvider = get(),
                blueprintProcessor = get(),
                activityLogStore = get(),
                blueprintPropertyResolver = get(),
                instanceRuntimeBuilder = get(),
                dockerContainerService = get(),
                dockerImageService = get(),
                instanceCommandExecutor = get(),
            )
        }

        factory<InstanceFileService> {
            InstanceFileService(
                instanceService = get(),
                dockerClient = get(),
                config = get(),
            )
        }

        single<InstanceEventsRedirector>(createdAtStart = true) {
            InstanceEventsRedirector(
                webSocketManager = get(),
                eventDispatcher = get(),
            )
        }
    }
