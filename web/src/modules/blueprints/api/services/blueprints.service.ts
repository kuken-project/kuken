import type { Blueprint } from "@/modules/blueprints/api/models/blueprint.model"
import httpService from "@/modules/platform/api/services/http.service"
import type { AxiosResponse } from "axios"

class BlueprintsService {

    async getBlueprint(id: string): Promise<Blueprint> {
        return httpService.get(`blueprints/${id}`)
            .then((res: AxiosResponse) => res.data as Blueprint);
    }
}

export default new BlueprintsService();