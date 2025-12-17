import {
    type NavigationGuard,
    type NavigationGuardNext,
    type RouteLocationNormalized
} from "vue-router"
import setupService from "@/modules/setup/api/services/setup.service.ts"
import { AxiosError } from "axios"
import { AUTH_LOGIN_ROUTE } from "@/modules/auth/auth.routes.ts"
import { SETUP_ROUTE } from "@/router.ts"

export const RequireSetupGuard: NavigationGuard = async (
    _to: RouteLocationNormalized,
    _from: RouteLocationNormalized,
    next: NavigationGuardNext
) => {
    if (_from.name == AUTH_LOGIN_ROUTE) {
        return next()
    }

    try {
        await setupService.getSetup()
    } catch (error) {
        if (!(error instanceof AxiosError)) throw error

        const isDueToSetupCompleted = error.response?.status === 423 /* Locked */

        if (isDueToSetupCompleted) {
            const isGoingToSetupPage = _to.name == SETUP_ROUTE

            // Trying to access setup but setup is already completed
            return isGoingToSetupPage ? next("/") : next()
        }

        return next(error)
    }

    return next({ name: SETUP_ROUTE })
}
