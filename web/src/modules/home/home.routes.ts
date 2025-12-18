import type { RouteRecordRaw } from "vue-router"
import { importPage } from "@/router"
import { BlueprintsRoutes } from "@/modules/blueprints/blueprints.routes.ts"

export const HOME_ROUTE = "home"

export const HomeRoutes: Array<RouteRecordRaw> = [
    {
        path: "/",
        name: HOME_ROUTE,
        component: importPage("home", "Home"),
        meta: {
            title: "Homepage"
        },
        children: [...BlueprintsRoutes]
    }
]
