import { defineStore } from "pinia"

type Token = string | null
type AuthState = { token: Token }

export const useAuthStore = defineStore("auth", {
    state: (): AuthState => ({ token: null }),
    getters: {
        /**
		 * Returns the current authorization token.
		 */
        getToken(): Token {
            return this.token
        }
    },
    actions: {
        // TODO Use access token model instead
        /**
		 * Updates the current authorization token
		 * @param {Token} newToken - The new authorization token.
		 */
        updateToken(newToken: Token) {
            this.token = newToken
        }
    }
})
