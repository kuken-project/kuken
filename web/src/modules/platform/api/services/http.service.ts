import type { AxiosInstance, AxiosPromise, AxiosRequestConfig, AxiosResponse } from "axios"
import Axios, { AxiosError } from "axios"
import configService from "@/modules/platform/api/services/config.service"
import { isUndefined } from "@/utils"
import logService from "@/modules/platform/api/services/log.service"
import { HttpError } from "@/modules/platform/api/models/error.model"
import type { KukenError } from "@/modules/platform/api/models/error.model"

class HttpService {
  readonly axios: AxiosInstance

  constructor() {
    this.axios = Axios.create({
      baseURL: configService.apiUrl,
      timeout: 5000,
      headers: {
        "Content-Type": "application/json"
      }
    })

    this.axios.interceptors.response.use(
      (response: AxiosResponse) => response,
      (error: AxiosError) => {
        const data: any = error.response?.data
        if (isUndefined(data?.code)) {
          logService.error("Unhandled HTTP error", error)
          throw error
        }

        throw new HttpError(data as KukenError)
      }
    )
  }

  get<T>(url: string, config?: AxiosRequestConfig): AxiosPromise<T> {
    return this.axios.get(url, config)
  }

  post<T>(url: string, data?: T, config?: AxiosRequestConfig): AxiosPromise<T> {
    return this.axios.post(url, data, config)
  }

  put<T>(url: string, data?: T, config?: AxiosRequestConfig): AxiosPromise<T> {
    return this.axios.put(url, data, config)
  }

  putForm<T>(url: string, data?: T, config?: AxiosRequestConfig): AxiosPromise<T> {
    return this.axios.putForm(url, data, config)
  }

  delete<T>(url: string, config?: AxiosRequestConfig): AxiosPromise<T> {
    return this.axios.delete(url, config)
  }

  patch<T>(url: string, data?: T): AxiosPromise<T> {
    return this.axios.patch(url, data)
  }
}

export default new HttpService()
