package gg.kuken.feature.setup

import org.koin.dsl.module

val SetupDI =
    module {
        factory {
            SetupService(
                accountService = get(),
                remoteConfigService = get(),
                accountPermissionRepository = get(),
                permissionRepository = get(),
                roleRepository = get(),
                blueprintService = get(),
            )
        }
    }
