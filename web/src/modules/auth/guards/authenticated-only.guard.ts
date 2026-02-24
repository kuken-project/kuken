import type { Account } from "@/modules/accounts/api/models/account.model"
import accountService from "@/modules/accounts/api/services/accounts.service"
import authService from "@/modules/auth/api/services/auth.service"
import logService from "@/modules/platform/api/services/log.service"
import { usePlatformStore } from "@/modules/platform/platform.store.ts"
import type { NavigationGuard, NavigationGuardNext, RouteLocationNormalized } from "vue-router"

export const AuthenticatedOnlyGuard: NavigationGuard = (
  to: RouteLocationNormalized,
  _from: RouteLocationNormalized,
  next: NavigationGuardNext
) => {
  const platformStore = usePlatformStore()

  // Missing backend info means app is initializing or backend info is missing (http error?)
  // In that case we can just skip here because App.vue will handle it properly
  if (!platformStore.hasBackendInfo) return next(undefined)

  if (accountService.isLoggedIn) return next()

  const localToken = authService.getLocalAccessToken()
  if (localToken === null) return gotoLoginOrProceed(to, next)

  authService
    .verify(localToken!)
    .then((account: Account | null) => {
      if (account === null) {
        return gotoLoginOrProceed(to, next)
      }

      accountService.updateAccount(account).finally(next)
    })
    .catch((error: Error) => {
      logService.debug("Unable to verify local token", error)
      gotoLoginOrProceed(to, next)
    })
}

function gotoLoginOrProceed(to: RouteLocationNormalized, next: NavigationGuardNext) {
  const isGoingToLogin = to.name === "login"
  if (isGoingToLogin) next()
  else next({ name: "login" })
}
