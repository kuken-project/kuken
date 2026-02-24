import { AccountsRoutes } from "@/modules/accounts/accounts.routes.ts"
import { AuthenticatedOnlyGuard } from "@/modules/auth/guards/authenticated-only.guard"
import { HomeRoutes } from "@/modules/home/home.routes"
import { OrganizationRoutes } from "@/modules/organization/organization.routes.ts"
import { UnitsRoutes } from "@/modules/units/units.routes.ts"
import { createRouter, createWebHistory } from "vue-router"

export function importPage(module: string, path: string): () => Promise<unknown> {
  const comps = import.meta.glob(`./modules/**/ui/pages/**/*.vue`)
  return comps[`./modules/${module}/ui/pages/${path}Page.vue`]!
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/login",
      name: "login",
      beforeEnter: [AuthenticatedOnlyGuard],
      component: () => import("@/modules/auth/ui/pages/LoginPage.vue"),
      meta: {
        title: "Log In"
      }
    },
    {
      path: "/",
      component: importPage("platform", "Root"),
      beforeEnter: [AuthenticatedOnlyGuard],
      children: [...HomeRoutes, ...UnitsRoutes, ...OrganizationRoutes, ...AccountsRoutes]
    },
    {
      path: "/setup",
      name: "setup",
      component: importPage("setup", "Setup"),
      meta: {
        title: "Set Up"
      }
    },
    {
      path: "/access-denied",
      name: "access-denied",
      component: importPage("platform", "AccessDenied"),
      beforeEnter: [AuthenticatedOnlyGuard]
    },
    {
      path: "/:pathMatch(.*)*",
      name: "not-found",
      component: importPage("platform", "NotFound"),
      beforeEnter: [AuthenticatedOnlyGuard]
    }
  ]
})

export default router
