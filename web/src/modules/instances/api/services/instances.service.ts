import type { VirtualFile } from "@/modules/instances/api/models/file.model.ts"
import type { Frame } from "@/modules/instances/api/models/frame.model.ts"
import type { Instance } from "@/modules/instances/api/models/instance.model.ts"
import { WebSocketOpCodes } from "@/modules/platform/api/models/websocket.response.ts"
import httpService from "@/modules/platform/api/services/http.service.ts"
import websocketService from "@/modules/platform/api/services/websocket.service.ts"
import type { AxiosResponse } from "axios"

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
  },

  async listFiles(instanceId: string, path: string): Promise<VirtualFile[]> {
    return httpService
      .get(`instances/${instanceId}/files/list?path=${path}`)
      .then((res: AxiosResponse) => res.data)
  },

  async getFileContents(instanceId: string, path: string): Promise<string> {
    return httpService
      .get(`instances/${instanceId}/files/contents`, {
        params: { path },
        responseType: "text"
      })
      .then((res: AxiosResponse) => res.data)
  },

  async replaceFileContents(instanceId: string, path: string, contents: string): Promise<string> {
    return httpService
      .put(`instances/${instanceId}/files/contents`, contents, {
        params: { path }
      })
      .then((res: AxiosResponse) => res.data)
  },

  async deleteFile(instanceId: string, path: string): Promise<string> {
    return httpService
      .delete(`instances/${instanceId}/files?path=${path}`)
      .then((res: AxiosResponse) => res.data)
  },

  async renameFile(
    instanceId: string,
    path: string,
    newName: string
  ): Promise<{
    updates: ["fileName"]
  }> {
    return httpService
      .patch(`instances/${instanceId}/files?path=${path}`, { newName })
      .then((res: AxiosResponse) => res.data)
  },

  async uploadFiles(instanceId: string, path: string, files: FormData): Promise<void> {
    return httpService
      .putForm(`instances/${instanceId}/files?path=${path}`, files, {
        onUploadProgress: (e) => {
          console.log("uploading...", e)
        }
      })
      .then((res: AxiosResponse) => res.data)
  }
} as const
