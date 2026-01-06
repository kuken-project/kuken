import type { BackendInfo } from "@/modules/platform/api/models/backend-info.ts"
import { usePlatformStore } from "@/modules/platform/platform.store"
import httpService from "@/modules/platform/api/services/http.service.ts"
import type { AxiosResponse } from "axios"

export default {
    async getInfo(): Promise<BackendInfo> {
        const store = usePlatformStore()
        if (store.hasBackendInfo) return store.getBackendInfo

        return httpService
            .get("/")
            .then((res: AxiosResponse) => res.data as BackendInfo)
            .then((info: BackendInfo) => {
                store.updateBackendInfo(info)
                return info
            })
    }
} as const
