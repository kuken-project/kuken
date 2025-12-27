export const WebSocketOpCodes = {
    InstanceUnavailable: 1 as const,
    InstanceLogsRequest: 2 as const,
} as const

export type WebSocketOp = (typeof WebSocketOpCodes)[keyof typeof WebSocketOpCodes]

export type WebSocketMessage = {
    o: WebSocketOp
    d?: unknown
}
