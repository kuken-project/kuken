import { BlueprintsRoutes } from "@/modules/blueprints/blueprints.routes.ts"
import { importPage } from "@/router"
import type { RouteRecordRaw } from "vue-router"

export const HOME_ROUTE = "home"

export const HomeRoutes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: HOME_ROUTE,
    component: importPage("home", "Home"),
    meta: {
      title: "Homepage"
    },
    children: [
      {
        path: "",
        name: "home.units",
        component: importPage("home", "HomeUnitList")
      },
      ...BlueprintsRoutes
    ]
  }
]
