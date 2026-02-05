import { AuthenticatedOnlyGuard } from "@/modules/auth/guards/authenticated-only.guard"
import { importPage } from "@/router"
import type { RouteRecordRaw } from "vue-router"

export const AUTH_LOGIN_ROUTE = "auth.login"

export const AuthRoutes: Array<RouteRecordRaw> = [
  {
    path: "/login",
    name: AUTH_LOGIN_ROUTE,
    beforeEnter: [AuthenticatedOnlyGuard],
    component: importPage("auth", "Login"),
    meta: {
      title: "Log In"
    }
  }
]
