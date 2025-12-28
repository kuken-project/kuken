import { createRouter, createWebHistory } from "vue-router"
import { AuthRoutes } from "@/modules/auth/auth.routes"
import { AuthenticatedOnlyGuard } from "@/modules/auth/guards/authenticated-only.guard"
import { HomeRoutes } from "@/modules/home/home.routes"
import { RequireSetupGuard } from "@/modules/setup/guards/require-setup.guard.ts"
import { InstancesRoutes } from "@/modules/instances/instances.routes.ts"
import { UnitsRoutes } from "@/modules/units/units.routes.ts"

export function importPage(module: string, path: string): () => Promise<unknown> {
    const comps = import.meta.glob(`./modules/**/ui/pages/**/*.vue`)
    return comps[`./modules/${module}/ui/pages/${path}Page.vue`]!
}

export const SETUP_ROUTE = "setup"

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        ...AuthRoutes,
        {
            path: "/",
            component: importPage("platform", "Root"),
            beforeEnter: [AuthenticatedOnlyGuard],
            children: [...HomeRoutes, ...InstancesRoutes, ...UnitsRoutes]
        },
        {
            path: "/setup",
            name: SETUP_ROUTE,
            component: importPage("setup", "Setup"),
            meta: {
                title: "Set Up"
            }
        },
        {
            path: "/access-denied",
            name: "access-denied",
            component: importPage("platform", "AccessDenied")
        }
    ]
})

router.beforeEach(RequireSetupGuard)

export default router
