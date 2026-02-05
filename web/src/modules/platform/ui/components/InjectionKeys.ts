import type { Tab } from "@/modules/platform/ui/components/tabs/VTabList.vue"
import type { InjectionKey } from "vue"

export const TabsInjectionKey: InjectionKey<{
  addTab(tab: Tab): void
  updateTab(id: string, payload: Partial<Tab>): void
  removeTab(id: string): void
  isTabActive(hash: string): boolean
}> = Symbol()
