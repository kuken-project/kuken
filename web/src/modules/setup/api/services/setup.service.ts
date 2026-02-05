import httpService from "@/modules/platform/api/services/http.service"
import type { Setup, SetupRequest } from "@/modules/setup/api/models/setup.model.ts"
import type { AxiosError, AxiosResponse } from "axios"

class SetupService {
  async getSetup(): Promise<Setup> {
    return httpService
      .get("setup")
      .then((res: AxiosResponse) => res.data as Setup)
      .catch((error: AxiosError) => {
        if (error.response?.status === 423 /* Locked */) return { completed: true } as Setup
        else throw error
      })
  }

  async completeSetup(request: SetupRequest): Promise<Setup> {
    return httpService.post("setup", request).then((res: AxiosResponse) => res.data as Setup)
  }
}

export default new SetupService()
