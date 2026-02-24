import type { BackendInfo } from "@/modules/platform/api/models/backend-info.ts"
import httpService from "@/modules/platform/api/services/http.service.ts"
import { usePlatformStore } from "@/modules/platform/platform.store"
import type { AxiosError, AxiosResponse } from "axios"

export default {
  async syncBackendInfo(): Promise<boolean> {
    const store = usePlatformStore()
    if (store.hasBackendInfo) return Promise.resolve(true)

    return httpService
      .get("/")
      .then((res: AxiosResponse) => {
        store.updateBackendInfo(res.data as BackendInfo)
        return true
      })
      .catch((error: AxiosError) => {
        if (error.response?.status === 423 /* Locked */) return false
        else throw error
      })
  }
} as const
