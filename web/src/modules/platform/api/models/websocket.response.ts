export const WebSocketOpCodes = {
    InstanceLogsRequest: 1 as const,
    InstanceUnavailable: 2 as const
} as const

export type WebSocketOp = (typeof WebSocketOpCodes)[keyof typeof WebSocketOpCodes]

export type WebSocketMessage = {
    o: WebSocketOp
    d?: unknown
}
