import type { BackendInfo } from "@/modules/platform/api/models/backend-info.ts"
import { isNull } from "@/utils"
import { defineStore } from "pinia"

type PlatformStore = { backendInfo: BackendInfo | null }

export const usePlatformStore = defineStore("platform", {
  state: (): PlatformStore => ({ backendInfo: null }),
  getters: {
    getBackendInfo(): BackendInfo {
      if (!this.hasBackendInfo) throw new Error("Missing backend information")

      return this.backendInfo!
    },
    hasBackendInfo(): boolean {
      return !isNull(this.backendInfo)
    }
  },
  actions: {
    updateBackendInfo(backendInfo: BackendInfo) {
      this.backendInfo = backendInfo
    }
  }
})
