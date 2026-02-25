package gg.kuken.feature.instance

import gg.kuken.core.docker.DockerContainerService
import gg.kuken.core.docker.DockerImageService
import gg.kuken.core.docker.DockerImageServiceImpl
import gg.kuken.core.docker.DockerNetworkService
import gg.kuken.feature.blueprint.BlueprintPropertyResolver
import gg.kuken.feature.instance.data.FilesystemBlueprintLockRepository
import gg.kuken.feature.instance.data.entity.InstanceRepositoryImpl
import gg.kuken.feature.instance.data.repository.BlueprintLockRepository
import gg.kuken.feature.instance.data.repository.InstanceRepository
import gg.kuken.feature.instance.service.BlueprintLockBuilder
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

        single<BlueprintLockRepository> {
            FilesystemBlueprintLockRepository(kukenConfig = get())
        }

        factory<BlueprintLockBuilder> {
            BlueprintLockBuilder(blueprintPropertyResolver = get())
        }

        factory<BlueprintPropertyResolver> {
            BlueprintPropertyResolver()
        }

        factory<InstanceRuntimeBuilder> {
            InstanceRuntimeBuilder(
                dockerClient = get(),
            )
        }

        factory<InstanceInstaller> {
            InstanceInstaller(
                dockerClient = get(),
                kukenConfig = get(),
            )
        }

        factory<DockerContainerService> {
            DockerContainerServiceImpl(
                dockerClient = get(),
                instanceInstaller = get(),
            )
        }

        factory<DockerImageService> {
            DockerImageServiceImpl(
                dockerClient = get(),
            )
        }

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
                blueprintLockBuilder = get(),
                blueprintLockRepository = get(),
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
