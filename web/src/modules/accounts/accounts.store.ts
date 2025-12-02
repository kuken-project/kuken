import { defineStore } from "pinia"
import { isNull } from "@/utils"
import type { Account } from "./api/models/account.model"

type AccountsState = {
	account: Account | null
}

export const useAccountsStore = defineStore("accounts", {
    state: (): AccountsState => ({ account: null }),
    getters: {
        isLoggedIn(): boolean {
            return !isNull(this.account)
        },
        getAccount(): Account {
            if (this.account == null) throw new Error("Cannot retrieve null account")

            return this.account
        }
    },
    actions: {
        updateAccount(account: Account | null) {
            this.account = account
        }
    }
})
