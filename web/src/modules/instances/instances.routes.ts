import type { RouteRecordRaw } from "vue-router"
import { importPage } from "@/router.ts"
import InstanceConsoleLayout from "@/modules/instances/ui/layouts/InstanceConsoleLayout.vue"

export const InstancesRoutes: Array<RouteRecordRaw> = [
    {
        path: "instances/:instanceId",
        name: "instance",
        props: true,
        component: importPage("instances", "InstanceMain"),
        meta: {
            title: "Instance"
        },
        children: [
            {
                path: "overview",
                name: "instance.overview",
                component: importPage("instances", "InstanceOverview"),
                meta: {
                    title: "Overview"
                }
            },
            {
                path: "console",
                name: "instance.console",
                component: importPage("instances", "InstanceConsole"),
                meta: {
                    title: "Console",
                    layout: InstanceConsoleLayout
                }
            }
        ]
    }
]
