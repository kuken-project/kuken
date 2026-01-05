import httpService from "@/modules/platform/api/services/http.service.ts"
import type { AxiosResponse } from "axios"
import type { Instance } from "@/modules/instances/api/models/instance.model.ts"

export default {
    async getInstance(instanceId: String): Promise<Instance> {
        return httpService
            .get(`instances/${instanceId}`)
            .then((res: AxiosResponse) => res.data as Instance)
    },

    async runInstanceCommand(instanceId: String, command: string): Promise<{ exitCode: number }> {
        return httpService
            .post(`instances/${instanceId}/command`, { command })
            .then((res: AxiosResponse) => res.data)
    }
} as const
