import httpService from "@/modules/platform/api/services/http.service.ts"
import type { AxiosResponse } from "axios"
import type { Instance } from "@/modules/instances/api/models/instance.model.ts"
import type { Frame } from "@/modules/instances/api/models/frame.model.ts"
import websocketService from "@/modules/platform/api/services/websocket.service.ts"
import { WebSocketOpCodes } from "@/modules/platform/api/models/websocket.response.ts"

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
    },

    async getLogs(
        instanceId: string,
        options: { limit?: number; before?: number; after?: number }
    ): Promise<{ frames: Frame[]; hasMore: boolean }> {
        return httpService
            .get(`instances/${instanceId}/logs`, { params: options })
            .then((res: AxiosResponse) => res.data)
    },

    async fetchLogs(
        instanceId: string,
        options: { afterSeqId?: number; beforeSeqId?: number; around?: number; batchSize: number }
    ): Promise<{ frames: Frame[]; hasMore: boolean }> {
        let unsubscribe: (() => void) | null = null
        return new Promise(async (resolve, _) => {
            unsubscribe = websocketService.listen(
                WebSocketOpCodes.InstanceLogsPacket,
                (payload: { frames: Frame[]; hasMore: boolean }) => {
                    console.log("InstanceLogsPacket Payload", payload)
                    resolve(payload)
                    unsubscribe?.()
                }
            )

            await websocketService.send(WebSocketOpCodes.InstanceLogsPacket, {
                iid: instanceId,
                before: options.beforeSeqId,
                after: options.afterSeqId,
                around: options.around,
                limit: options.batchSize
            })
        })
    }
} as const
