package gg.kuken.feature.rbac

import gg.kuken.feature.rbac.repository.AccountPermissionRepository
import gg.kuken.feature.rbac.repository.PermissionRepository
import gg.kuken.feature.rbac.repository.RoleRepository
import gg.kuken.feature.rbac.service.PermissionService
import org.koin.dsl.module

val RBACDI =
    module {
        single<AccountPermissionRepository>(createdAtStart = true) {
            AccountPermissionRepository(database = get())
        }

        single<PermissionRepository>(createdAtStart = true) {
            PermissionRepository(database = get())
        }

        single<RoleRepository>(createdAtStart = true) {
            RoleRepository(database = get())
        }

        factory {
            PermissionService(
                permissionRepository = get(),
                roleRepository = get(),
                accountPermissionRepository = get(),
            )
        }
    }
