package gg.kuken.feature.account

import gg.kuken.feature.account.repository.AccountRepository
import gg.kuken.feature.account.repository.AccountsRepositoryImpl
import org.koin.dsl.module

val AccountDI =
    module {
        single<AccountRepository> {
            AccountsRepositoryImpl(database = get())
        }
        single<AccountService> {
            AccountServiceImpl(
                identityGeneratorService = get(),
                accountsRepository = get(),
                hashAlgorithm = get(),
                eventDispatcher = get(),
            )
        }
    }
