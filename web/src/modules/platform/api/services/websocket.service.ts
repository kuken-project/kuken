import { isUndefined } from "@/utils"
import {
    type WebSocketMessage,
    type WebSocketOp
} from "@/modules/platform/api/models/websocket.response"
import type { Logger } from "@/modules/platform/api/services/log.service"
import logService from "@/modules/platform/api/services/log.service"
import configService from "@/modules/platform/api/services/config.service"

type WebSocketListener = (arg0: any) => void

class WebSocketService {
    private readonly logger: Logger = logService.create("Gateway")
    private readonly listeners = new Map<number, WebSocketListener[]>()
    private ws?: WebSocket

    tryConnect(onConnect: () => void, onFailure?: () => void): void {
        if (this.isConnected()) return

        const url = configService.gatewayUrl
        this.logger.info("Connecting to", url)
        this.ws = new WebSocket(url)

        const start = performance.now()
        this.ws.onopen = () => {
            const end = performance.now()
            this.logger.info(`Connected in ${end - start}ms`)
            onConnect()
        }
        this.ws.onerror = () => {
            onFailure?.()
        }
        this.ws.onmessage = (e: MessageEvent) => {
            this.logger.debug("Message received:", e.data)

            const data = JSON.parse(e.data)

            if (isUndefined(data.o)) {
                this.logger.error("Missing message op code.")
                return
            }

            const listenersForOp = this.listeners.get(data.o)
            if (!listenersForOp) return

            for (const listener of listenersForOp) listener(data.d)
        }
    }

    async send(code: WebSocketOp, payload: any): Promise<void> {
        if (isUndefined(this.ws) || this.ws.readyState !== WebSocket.OPEN) {
            this.logger.debug("Waiting connection be established to send message...")
            await this.awaitConnect()
        }

        const message: WebSocketMessage = { o: code, d: payload }

        this.ws?.send(JSON.stringify(message))
        this.logger.debug("Message sent:", message)
    }

    awaitConnect(): Promise<void> {
        return new Promise((resolve, reject) => {
            if (this.isConnected()) return resolve()

            this.tryConnect(resolve, reject)
        })
    }

    isConnected(): boolean {
        return !isUndefined(this.ws) && this.ws.readyState === WebSocket.OPEN
    }

    listen(op: number, listener: WebSocketListener): () => void {
        if (!this.listeners.has(op)) this.listeners.set(op, [])

        this.listeners.get(op)?.push(listener)
        this.logger.debug(`Added listener for op ${op}`)

        return () => {
            this.listeners.get(op)?.splice(this.listeners.get(op)?.indexOf(listener)!, 1)
            this.logger.debug(`Removed listener for op ${op}`)
        }
    }

    close(): void {
        this.ws?.close(1000)
        this.ws = undefined
    }
}

export default new WebSocketService()
