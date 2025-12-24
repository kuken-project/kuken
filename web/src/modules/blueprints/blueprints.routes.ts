import type { RouteRecordRaw } from "vue-router"
import { importPage } from "@/router"

const MODULE = "blueprints"
export const BLUEPRINTS_ROUTE = "blueprints"

export const BlueprintsRoutes: Array<RouteRecordRaw> = [
    {
        path: "blueprints",
        name: BLUEPRINTS_ROUTE,
        component: importPage(MODULE, "Blueprints"),
        meta: {
            title: "Game Directory"
        },
        children: [
            {
                path: "",
                name: "blueprints.home",
                component: importPage(MODULE, "home/BlueprintsHome")
            },
            {
                path: ":blueprintId/details",
                props: true,
                name: "blueprints.details",
                component: importPage(MODULE, "detail/BlueprintDetail"),
                meta: {
                    title: "Blueprint Details"
                }
            }
        ]
    }
]
