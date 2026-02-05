import type { Blueprint } from "@/modules/blueprints/api/models/blueprint.model"
import type {
  BlueprintSpec,
  ResolveBlueprintResponse
} from "@/modules/blueprints/api/models/blueprint.spec.model.ts"
import type {
  ImportBlueprintResponse,
  ProcessBlueprintResponse
} from "@/modules/blueprints/api/models/import.model.ts"
import type { ProcessBlueprintRequest } from "@/modules/blueprints/api/models/process.model.ts"
import httpService from "@/modules/platform/api/services/http.service"
import type { AxiosResponse } from "axios"

export default {
  async listReadyToUseBlueprints(): Promise<Blueprint[]> {
    return httpService
      .get(`blueprints`)
      .then((res: AxiosResponse) => res.data.blueprints as Blueprint[])
  },

  async getBlueprint(id: string): Promise<Blueprint> {
    return httpService.get(`blueprints/${id}`).then((res: AxiosResponse) => res.data as Blueprint)
  },

  async importBlueprint(url: string): Promise<ImportBlueprintResponse> {
    return httpService
      .post("/blueprints/import", { source: url })
      .then((res: AxiosResponse) => res.data as Blueprint)
  },

  async resolveBlueprint(blueprintId: string): Promise<ResolveBlueprintResponse> {
    return httpService
      .get(`/blueprints/${blueprintId}/resolve`)
      .then((res: AxiosResponse) => res.data)
  },

  async processBlueprint(
    blueprintId: string,
    options: ProcessBlueprintRequest
  ): Promise<ProcessBlueprintResponse> {
    return httpService
      .post(`/blueprints/${blueprintId}/process`, options)
      .then((res: AxiosResponse) => res.data as BlueprintSpec)
  }
}
