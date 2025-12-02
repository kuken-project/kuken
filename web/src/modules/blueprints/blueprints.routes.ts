import type { RouteRecordRaw } from "vue-router"
import { importPage } from "@/router"

const MODULE = "blueprints"
export const BLUEPRINTS_ROUTE = "blueprints"

export const BlueprintsRoutes: Array<RouteRecordRaw> = [
    {
        path: "blueprints",
        name: BLUEPRINTS_ROUTE,
        component: importPage(MODULE, "Blueprints"),
        children: [
            {
                path: "",
                component: importPage(MODULE, "home/BlueprintsHome")
            },
            {
                path: ":blueprintId",
                props: true,
                component: importPage(MODULE, "info/BlueprintsInfo")
            }
        ]
    }

]
