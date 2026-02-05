import type { Frame } from "@/modules/instances/api/models/frame.model.ts"
import instancesService from "@/modules/instances/api/services/instances.service.ts"
import { computed, ref } from "vue"

export type ConsoleLogsResponse = {
  frames: Frame[]
  hasMore: boolean
}

export type UseConsoleLoaderOptions = {
  instanceId: string
  batchSize: number
}

export function useConsoleLoader(options: UseConsoleLoaderOptions) {
  const { instanceId, batchSize } = options

  const windowHours = ref(6)
  const windowStart = ref<number | null>(null) // null = tempo real
  const isRealtime = computed(() => windowStart.value === null)

  async function loadLogs(): Promise<Frame[]> {
    const now = Date.now()

    let since: number
    let until: number

    if (windowStart.value === null) {
      since = now - windowHours.value * 60 * 60 * 1000
      until = now
    } else {
      since = windowStart.value
      until = windowStart.value + windowHours.value * 60 * 60 * 1000
    }

    const response = await instancesService.getLogs(instanceId, {
      after: since,
      before: until
    })

    return response.frames
  }

  function goToPrevious() {
    const currentStart = windowStart.value ?? Date.now()
    windowStart.value = currentStart - windowHours.value * 60 * 60 * 1000
  }

  function goToNext() {
    if (windowStart.value === null) return

    const nextStart = windowStart.value + windowHours.value * 60 * 60 * 1000

    if (nextStart + windowHours.value * 60 * 60 * 1000 >= Date.now()) {
      windowStart.value = null
    } else {
      windowStart.value = nextStart
    }
  }

  function goToRealtime() {
    windowStart.value = null
  }

  function goToDate(timestamp: number) {
    windowStart.value = timestamp - (windowHours.value * 60 * 60 * 1000) / 2
  }

  return {
    isRealtime,
    loadLogs,
    goToPrevious,
    goToNext,
    goToRealtime,
    goToDate
  }
}
