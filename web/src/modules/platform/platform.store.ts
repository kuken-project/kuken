import { defineStore } from "pinia"
import type { ServerInfo } from "@/modules/platform/api/models/server-info"
import { isNull } from "@/utils"

type PlatformStore = { serverInfo: ServerInfo | null }

export const usePlatformStore = defineStore("platform", {
    state: (): PlatformStore => ({ serverInfo: null }),
    getters: {
        getServerInfo(): ServerInfo {
            if (!this.hasServerInfo)
                throw new Error("Missing system information")

            return this.serverInfo!
        },
        hasServerInfo(): boolean {
            return !isNull(this.serverInfo);
        }
    },
    actions: {
        updateServerInfo(serverInfo: ServerInfo) {
            this.serverInfo = serverInfo;
        }
    }
})