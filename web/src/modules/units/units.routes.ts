import { InstancesRoutes } from "@/modules/instances/instances.routes.ts"
import UnitMain from "@/modules/units/ui/components/UnitMain.vue"
import { importPage } from "@/router.ts"
import type { RouteRecordRaw } from "vue-router"

export const UnitsRoutes: Array<RouteRecordRaw> = [
  {
    path: "new/:blueprint?",
    name: "units.create",
    props: true,
    component: importPage("units", "create-unit/CreateUnit"),
    meta: {
      title: "Create new"
    }
  },
  {
    path: "servers/:unitId",
    props: true,
    component: UnitMain,
    meta: {
      title: "Server"
    },
    children: [
      ...InstancesRoutes,
      {
        path: "",
        name: "unit",
        component: importPage("units", "overview/UnitOverview")
      }
    ]
  }
]
