import type { Frame } from "@/modules/instances/api/models/frame.model.ts"
import { ref } from "vue"
import instancesService from "@/modules/instances/api/services/instances.service.ts"

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

    const isLoadingOlder = ref(false)
    const isLoadingNewer = ref(false)
    const hasOlderLogs = ref(true)
    const hasNewerLogs = ref(false)

    async function loadOlderLogs(beforeTimestamp: number): Promise<Frame[]> {
        if (isLoadingOlder.value || !hasOlderLogs.value) return []

        isLoadingOlder.value = true

        try {
            const response = await instancesService.getLogs(instanceId, {
                after: beforeTimestamp - 10 * 60 * 1000,
                before: beforeTimestamp
            })
            hasOlderLogs.value = response.hasMore

            return response.frames
        } catch (error) {
            // TODO Use logger service with useLogger instead
            console.error("Failed to load older logs", beforeTimestamp, error)
            return []
        } finally {
            isLoadingOlder.value = false
        }
    }

    async function loadNewerLogs(afterTimestamp: number): Promise<Frame[]> {
        if (isLoadingNewer.value || !hasNewerLogs.value) return []

        isLoadingNewer.value = true

        try {
            const response = await instancesService.getLogs(instanceId, {
                after: afterTimestamp,
                limit: batchSize
            })
            hasNewerLogs.value = response.hasMore

            return response.frames
        } catch (error) {
            // TODO Use logger service with useLogger instead
            console.error("Failed to load newer logs", afterTimestamp, error)
            return []
        } finally {
            isLoadingNewer.value = false
        }
    }

    async function loadAroundTimestamp(timestamp: number): Promise<Frame[]> {
        try {
            const response = await instancesService.fetchLogs(instanceId, {
                around: timestamp,
                batchSize
            })

            hasOlderLogs.value = true
            hasNewerLogs.value = true
            return response.frames
        } catch (error) {
            // TODO Use logger service with useLogger instead
            console.error("Failed to load logs aroung timestamp", timestamp, error)
            return []
        }
    }

    function setHasOlderLogs(value: boolean) {
        hasOlderLogs.value = value
    }

    function setHasNewerLogs(value: boolean) {
        hasNewerLogs.value = value
    }

    function resetToRealtime() {
        hasNewerLogs.value = false
    }

    return {
        isLoadingOlder,
        isLoadingNewer,
        hasOlderLogs,
        hasNewerLogs,
        loadOlderLogs,
        loadNewerLogs,
        loadAroundTimestamp,
        setHasOlderLogs,
        setHasNewerLogs,
        resetToRealtime
    }
}
