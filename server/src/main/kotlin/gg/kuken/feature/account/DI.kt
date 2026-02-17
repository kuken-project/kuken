package gg.kuken.feature.account

import gg.kuken.feature.account.repository.AccountRepository
import gg.kuken.feature.account.repository.AccountsRepositoryImpl
import org.koin.dsl.module

val AccountDI =
    module {
        single<AccountRepository>(createdAtStart = true) {
            AccountsRepositoryImpl(database = get())
        }
        factory<AccountService> {
            AccountServiceImpl(
                identityGeneratorService = get(),
                accountsRepository = get(),
                hashAlgorithm = get(),
                eventDispatcher = get(),
            )
        }
    }
