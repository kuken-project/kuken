import type { Blueprint } from "@/modules/blueprints/api/models/blueprint.model"
import httpService from "@/modules/platform/api/services/http.service"
import type { AxiosResponse } from "axios"
import type { ImportBlueprintResponse } from "@/modules/blueprints/api/models/import.model.ts"

export default {
    async listReadyToUseBlueprints(): Promise<Blueprint[]> {
        return httpService
            .get(`blueprints`)
            .then((res: AxiosResponse) => res.data.blueprints as Blueprint[])
    },

    async getBlueprint(id: string): Promise<Blueprint> {
        return httpService
            .get(`blueprints/${id}`)
            .then((res: AxiosResponse) => res.data as Blueprint)
    },

    async importBlueprint(url: string): Promise<ImportBlueprintResponse> {
        return httpService
            .post("/blueprints/import", { url, type: "remote" })
            .then((res: AxiosResponse) => res.data as Blueprint)
    }
}
