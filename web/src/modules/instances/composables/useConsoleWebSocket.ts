import type { Frame } from "@/modules/instances/api/models/frame.model.ts"
import { ref } from "vue"
import websocketService from "@/modules/platform/api/services/websocket.service.ts"
import { WebSocketOpCodes } from "@/modules/platform/api/models/websocket.response.ts"

export type UseConsoleWebSocketOptions = {
    instanceId: string
    onFrame: (frame: Frame) => void
    onEnd?: () => void
}

const NullVoidFn = null as (() => void) | null

export function useConsoleWebSocket(options: UseConsoleWebSocketOptions) {
    const { instanceId, onFrame, onEnd } = options

    const isConnected = ref(false)
    const logsEnded = ref(false)

    let unsubscribeLogsStart = NullVoidFn
    let unsubscribeLogsFrames = NullVoidFn
    let unsubscribeLogsEnd = NullVoidFn
    let unsubscribeInstanceStart = NullVoidFn

    function subscribe(since: number) {
        if (since > 0) {
            console.log(`Subscribing console logs since ${new Date(since).toISOString()}`)
        } else {
            console.log(`Subscribing console logs from the beggining`)
        }

        unsubscribeLogsStart = websocketService.listen(
            WebSocketOpCodes.InstanceLogsRequestStarted,
            () => {
                isConnected.value = true
            }
        )

        unsubscribeLogsFrames = websocketService.listen(
            WebSocketOpCodes.InstanceLogsRequestFrame,
            (frame: Frame) => {
                isConnected.value = true
                onFrame(frame)
            }
        )

        unsubscribeLogsEnd = websocketService.listen(
            WebSocketOpCodes.InstanceLogsRequestFinished,
            () => {
                logsEnded.value = true
                isConnected.value = false
                onEnd?.()
            }
        )

        unsubscribeInstanceStart = websocketService.listen(WebSocketOpCodes.InstanceStarted, () => {
            reconnect()
        })

        websocketService.send(WebSocketOpCodes.InstanceLogsRequest, {
            iid: instanceId,
            since
        })
    }

    function unsubscribe(reason: String) {
        console.log("useConsoleWebSocket: unsubscribe()", reason)
        unsubscribeLogsStart?.()
        unsubscribeLogsFrames?.()
        unsubscribeLogsEnd?.()
        unsubscribeInstanceStart?.()
    }

    function reconnect() {
        logsEnded.value = false
        unsubscribe("reconnect")
        subscribe(0)
        console.log("useConsoleWebSocket: reconnect()")
    }

    return {
        isConnected,
        logsEnded,
        subscribe,
        unsubscribe,
        reconnect
    }
}
