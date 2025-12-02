import type { ServerInfo } from "@/modules/platform/api/models/server-info"
import { usePlatformStore } from "@/modules/platform/platform.store"
import type { Logger } from "@/modules/platform/api/services/log.service"
import logService from "@/modules/platform/api/services/log.service"

class ServerService {

    private readonly logger: Logger = logService.create("backend")

    async getInfo(): Promise<ServerInfo> {
        const store = usePlatformStore()
        if (store.hasServerInfo)
            return store.getServerInfo

        const value = { setup: true } as ServerInfo
        this.logger.debug(value);
        store.updateServerInfo(value)
        return value
        // return httpService.get("/").then((res: AxiosResponse) => res.data as SystemInfo);
    }
}

export default new ServerService()