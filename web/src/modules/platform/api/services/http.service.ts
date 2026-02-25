import authService from "@/modules/auth/api/services/auth.service.ts"
import type { KukenError } from "@/modules/platform/api/models/error.model"
import { HttpError } from "@/modules/platform/api/models/error.model"
import configService from "@/modules/platform/api/services/config.service"
import { isUndefined } from "@/utils"
import type { AxiosInstance, AxiosPromise, AxiosRequestConfig } from "axios"
import Axios, { AxiosError } from "axios"

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
      (response) => response,
      (error: AxiosError) => {
        const data = error.response?.data as { code?: string }
        if (!isUndefined(data?.code)) {
          throw new HttpError(data as KukenError)
        }

        throw error
      }
    )

    this.axios.interceptors.request.use((request) => {
      const localToken = authService.getLocalAccessToken()
      if (localToken !== null) request.headers!["Authorization"] = `Bearer ${localToken.token}`
      return request
    })
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
