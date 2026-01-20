export type Frame = {
    value: string
    length: number
    stream: {
        code: number
        name: "STDOUT" | "STDERR"
    }
    timestamp: number
    persistentId: string
    seqId: number
}
