import type { NavigationGuard, NavigationGuardNext, RouteLocationNormalized } from "vue-router"
import accountService from "@/modules/accounts/api/services/accounts.service"
import authService from "@/modules/auth/api/services/auth.service"
import { isNull } from "@/utils"
import type { Account } from "@/modules/accounts/api/models/account.model"
import logService from "@/modules/platform/api/services/log.service"
import { AUTH_LOGIN_ROUTE } from "@/modules/auth/auth.routes"

export const AuthenticatedOnlyGuard: NavigationGuard = (
    to: RouteLocationNormalized,
    _from: RouteLocationNormalized,
    next: NavigationGuardNext
) => {
    if (accountService.isLoggedIn) return next()

    const isGoingToLogin = to.name === AUTH_LOGIN_ROUTE
    const localToken = authService.getLocalAccessToken()
    if (isNull(localToken))
        return isGoingToLogin ? next() : next({ name: AUTH_LOGIN_ROUTE })

    authService
        .verify(localToken!)
        .then(async (account: Account) => {
            await accountService.updateAccount(account)
            next()
        })
        .catch((error: Error) => {
            logService.debug("Unable to verify local token", error)
            isGoingToLogin ? next() : next({ name: AUTH_LOGIN_ROUTE })
        })
}
