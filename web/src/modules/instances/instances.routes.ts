import { importPage } from "@/router.ts"
import type { RouteRecordRaw } from "vue-router"

export const InstancesRoutes: Array<RouteRecordRaw> = [
  {
    path: "instances/:instanceId",
    name: "instance",
    props: true,
    component: importPage("instances", "InstanceMain"),
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
          layout: null
        }
      },
      {
        path: "files",
        component: importPage("instances", "files/InstanceFileSystem"),
        meta: {
          title: "File System"
        },
        children: [
          {
            path: "",
            name: "instance.files",
            props: true,
            component: importPage("instances", "files/browser/InstanceFileBrowser"),
            meta: {
              title: "File Browser"
            }
          },
          {
            path: "editor",
            name: "instance.file.editor",
            props: true,
            component: importPage("instances", "files/editor/InstanceFileEditor"),
            meta: {
              title: "File Contents"
            }
          }
        ]
      }
    ]
  }
]
