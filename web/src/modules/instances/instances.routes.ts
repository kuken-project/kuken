import type {RouteRecordRaw} from "vue-router";
import {importPage} from "@/router.ts";

export const InstancesRoutes: Array<RouteRecordRaw> = [
    {
        path: "instances/:instanceId",
        name: "instance",
        props: true,
        component: importPage("instances", "InstanceMain"),
        meta: {
            title: "Instance"
        },
        children: [{
            path: "overview",
            name: "instance.overview",
            component: importPage("instances", "InstanceOverview"),
            meta: {
                title: "Overview"
            }
        }]
    }
]