package gg.kuken.feature.account

import gg.kuken.core.EventDispatcher
import gg.kuken.core.security.Hash
import gg.kuken.feature.account.entity.toDomain
import gg.kuken.feature.account.model.Account
import gg.kuken.feature.account.repository.AccountRepository
import gg.kuken.feature.rbac.service.PermissionService
import kotlin.time.Clock
import kotlin.uuid.Uuid

interface AccountService {
    suspend fun listAccounts(): List<Account>

    suspend fun getAccount(id: Uuid): Account?

    suspend fun getAccountByEmail(email: String): Account?

    suspend fun getAccountAndHash(email: String): Pair<Account, String>?

    suspend fun createAccount(
        email: String,
        password: String,
    ): Account

    suspend fun deleteAccount(id: Uuid)

    suspend fun existsAnyAccount(): Boolean
}

class AccountServiceImpl(
    private val identityGeneratorService: IdentityGeneratorService,
    private val accountsRepository: AccountRepository,
    private val hashAlgorithm: Hash,
    private val eventDispatcher: EventDispatcher,
    private val permissionService: PermissionService,
) : AccountService {
    override suspend fun listAccounts(): List<Account> = accountsRepository.findAll().map { entity -> entity.toDomain() }

    override suspend fun getAccount(id: Uuid): Account? = accountsRepository.findById(id)?.toDomain()

    override suspend fun getAccountByEmail(email: String): Account? = accountsRepository.findByEmail(email)?.toDomain()

    override suspend fun getAccountAndHash(email: String): Pair<Account, String>? {
        // TODO optimize it
        val account = accountsRepository.findByEmail(email)?.toDomain() ?: return null
        val hash = accountsRepository.findHashByEmail(email) ?: return null

        return account to hash
    }

    override suspend fun createAccount(
        email: String,
        password: String,
    ): Account {
        if (accountsRepository.existsByEmail(email)) {
            error("Conflict") // TODO Conflict exception
        }

        val now = Clock.System.now()
        val account =
            Account(
                id = identityGeneratorService.generate(),
                displayName = null,
                email = email,
                createdAt = now,
                updatedAt = now,
                lastLoggedInAt = null,
                avatar = null,
            )

        val hash = hashAlgorithm.hash(password.toCharArray())
        accountsRepository.addAccount(account, hash)
        eventDispatcher.dispatch(AccountCreatedEvent(account.id))
        return account
    }

    override suspend fun deleteAccount(id: Uuid) {
        accountsRepository.deleteAccount(id)
        eventDispatcher.dispatch(AccountDeletedEvent(id))
    }

    override suspend fun existsAnyAccount(): Boolean = accountsRepository.existsAnyAccount()
}
