import type { Logger } from "@/modules/platform/api/services/log.service"
import { useAccountsStore } from "../../accounts.store"
import logService from "@/modules/platform/api/services/log.service"
import type { Account } from "../models/account.model"
import httpService from "@/modules/platform/api/services/http.service"
import type { AxiosResponse } from "axios"

class AccountsService {
    private readonly logger!: Logger

    constructor() {
        this.logger = logService.create(AccountsService.name)
    }

    get isLoggedIn(): boolean {
        return useAccountsStore().isLoggedIn
    }

    async listAccounts(): Promise<Account[]> {
        return httpService.get("accounts")
            .then((res: AxiosResponse) => res.data as Account[]);
    }

    async updateAccount(account: Account): Promise<void> {
        this.logger.debug("Account updated", account)
        useAccountsStore().updateAccount(account)
    }
}

export default new AccountsService()
