import httpService from "@/modules/platform/api/services/http.service.ts"
import type { CreateUnitRequest } from "@/modules/units/api/models/create-unit.model.ts"
import type { Unit } from "@/modules/units/api/models/unit.model.ts"
import type { AxiosResponse } from "axios"

export default {
  async listUnits(): Promise<Unit[]> {
    return httpService.get(`units`).then((res: AxiosResponse) => res.data as Unit[])
  },

  async createUnit(options: CreateUnitRequest): Promise<Unit> {
    return httpService
      .post(`units`, options, {
        timeout: 15000
      })
      .then((res: AxiosResponse) => res.data as Unit)
  },

  async getUnit(unitId: string): Promise<Unit> {
    return httpService.get(`units/${unitId}`).then((res: AxiosResponse) => res.data as Unit)
  }
} as const
