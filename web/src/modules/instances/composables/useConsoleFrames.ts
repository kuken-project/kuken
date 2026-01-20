import { computed, ref } from "vue"
import type { Frame } from "@/modules/instances/api/models/frame.model.ts"

export type UseConsoleFramesOptions = {
    maxFrames: number
}

export function useConsoleFrames(options: UseConsoleFramesOptions) {
    const frames = ref<Frame[]>([])
    const seenIds = ref(new Set<string>())
    const seqOffsetId = ref(0)

    const oldestSeqId = computed(() => (frames.value.length > 0 ? frames.value[0]!.seqId : null))
    const newestSeqId = computed(() =>
        frames.value.length > 0 ? frames.value[frames.value.length - 1]!.seqId : null
    )

    function addFrame(frame: Frame): { trimmedOld: boolean } {
        if (seenIds.value.has(frame.persistentId)) return { trimmedOld: false }

        frame.seqId = seqOffsetId.value + frame.seqId
        seenIds.value.add(frame.persistentId)
        frames.value.push(frame)

        let trimmedOld = false
        if (frames.value.length > options.maxFrames) {
            const removed = frames.value.splice(0, frames.value.length - options.maxFrames)
            removed.forEach((frame) => seenIds.value.delete(frame.persistentId))
            trimmedOld = true
        }

        return { trimmedOld }
    }

    function preprendFrames(newFrames: Frame[]): { trimmedNew: boolean } {
        const uniqueFrames = newFrames.filter((f) => !seenIds.value.has(f.persistentId))

        uniqueFrames.forEach((f) => seenIds.value.add(f.persistentId))
        frames.value.unshift(...uniqueFrames)

        frames.value.forEach((f, index) => {
            f.seqId = index + 1
        })

        seqOffsetId.value = frames.value.length

        let trimmedNew = false
        if (frames.value.length > options.maxFrames) {
            const removed = frames.value.splice(options.maxFrames)
            removed.forEach((frame) => seenIds.value.delete(frame.persistentId))
            trimmedNew = true
        }

        return { trimmedNew }
    }

    function appendFrames(newFrames: Frame[]): { trimmedOld: boolean } {
        const uniqueFrames = newFrames.filter((frame) => !seenIds.value.has(frame.persistentId))
        const maxCurrentSeqId = frames.value[frames.value.length - 1]?.seqId ?? 0

        uniqueFrames.forEach((frame, index) => {
            frame.seqId = maxCurrentSeqId + index + 1
            seenIds.value.add(frame.persistentId)
        })

        frames.value.push(...uniqueFrames)

        let trimmedOld = false
        if (frames.value.length > options.maxFrames) {
            const removed = frames.value.splice(0, frames.value.length - options.maxFrames)
            removed.forEach((frame) => seenIds.value.delete(frame.persistentId))
            trimmedOld = true
        }

        return { trimmedOld }
    }

    function setFrames(newFrames: Frame[]) {
        frames.value = newFrames
        seenIds.value.clear()
        newFrames.forEach((frame) => seenIds.value.add(frame.persistentId))

        if (newFrames.length > 0) {
            seqOffsetId.value = newFrames[newFrames.length - 1]?.seqId ?? 0
        }
    }

    function clear() {
        frames.value = []
        seenIds.value.clear()
    }

    function trimToRecent(count: number) {
        if (frames.value.length > count) {
            const kept = frames.value.slice(-count)
            frames.value = kept
            seenIds.value.clear()
            kept.forEach((frame) => seenIds.value.add(frame.persistentId))
        }
    }

    function findByPersistentId(persistentId: string): number {
        return frames.value.findIndex((frame) => frame.persistentId === persistentId)
    }

    return {
        frames,
        oldestSeqId,
        newestSeqId,
        addFrame,
        preprendFrames,
        appendFrames,
        setFrames,
        clear,
        trimToRecent,
        findByPersistentId
    }
}
