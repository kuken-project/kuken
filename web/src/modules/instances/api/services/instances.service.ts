import httpService from "@/modules/platform/api/services/http.service.ts"
import type { AxiosResponse } from "axios"
import type { Instance } from "@/modules/instances/api/models/instance.model.ts"

class InstancesService {
    async getInstance(instanceId: String): Promise<Instance> {
        return httpService
            .get(`instances/${instanceId}`)
            .then((res: AxiosResponse) => res.data as Instance)
    }
}

export default () => new InstancesService()
