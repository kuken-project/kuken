import { importPage } from "@/router.ts"
import type { RouteRecordRaw } from "vue-router"

export const AccountsRoutes: Array<RouteRecordRaw> = [
  {
    path: "profile",
    component: importPage("accounts", "Profile"),
    children: [
      {
        path: "",
        name: "profile",
        component: () => import("@/modules/accounts/ui/pages/overview/ProfileOverviewPage.vue")
      }
    ]
  }
]
