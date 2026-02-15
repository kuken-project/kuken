export type Frame = FrameLike | ConsoleFrame | ActivityFrame

export type FrameLike = {
  msg: string
  ts: number
  seqId: number
  persistentId: string
}

export type ConsoleFrame = FrameLike & {
  type: "console"
  stream: {
    code: number
    name: "STDOUT" | "STDERR"
  }
}

export type ActivityFrame = FrameLike & {
  type: "activity"
  activity: "install" | "update" | "backup" | "restore"
  step: string
  progress: number
}
