import type { Account } from "@/modules/accounts/api/models/account.model"
import httpService from "@/modules/platform/api/services/http.service"
import localStorageService from "@/modules/platform/api/services/local-storage.service"
import logService, { type Logger } from "@/modules/platform/api/services/log.service"
import type { AxiosError, AxiosResponse } from "axios"
import type { AccessToken } from "../models/access-token.model"

export const AUTHORIZATION_TOKEN_KEY = "token"

class AuthService {
  private readonly logger!: Logger

  constructor() {
    this.logger = logService.create(AuthService.name)
  }

  getLocalAccessToken(): AccessToken | null {
    return localStorageService.get(AUTHORIZATION_TOKEN_KEY)
  }

  resetLocalAccessToken(): void {
    localStorageService.remove(AUTHORIZATION_TOKEN_KEY)
  }

  async login(username: string, password: string): Promise<void> {
    return httpService
      .post("auth/login", { username, password })
      .then((res: AxiosResponse) => {
        const accessToken = { token: res.data.token } as AccessToken
        this.logger.debug("Login performed", accessToken)
        localStorageService.set(AUTHORIZATION_TOKEN_KEY, accessToken)
      })
      .catch((error: AxiosError) => {
        this.logger.debug("Failed to perform login", error)
        throw error
      })
  }

  async verify(accessToken: AccessToken): Promise<Account | null> {
    return httpService
      .get("auth", {
        headers: { Authorization: `Bearer ${accessToken.token}` }
      })
      .then((res: AxiosResponse) => {
        const entity = res.data
        return {
          id: entity.accountId,
          email: entity.email,
          createdAt: entity["created-at"],
          updatedAt: entity["updated-at"],
          permissions: entity["permissions"]
        } as Account
      })
      .catch((error: AxiosError) => {
        if (error.response?.status === 401 /* Unauthorized */) {
          console.error("Not authorized :(")
          return null
        } else {
          throw error
        }
      })
  }
}

export default new AuthService()
