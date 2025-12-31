<script setup lang="ts">
import websocketService from "@/modules/platform/api/services/websocket.service.ts"
import { WebSocketOpCodes } from "@/modules/platform/api/models/websocket.response.ts"
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from "vue"
import { useElementSize, useScroll } from "@vueuse/core"

interface Frame {
    value: string
    length: number
    stream: {
        code: number
        name: "STDOUT" | "STDERR"
    }
    timestamp?: number
}

const props = defineProps<{ instanceId: string }>()
const frames = ref<Frame[]>([])
const maxFrames = 10000
const isPaused = ref(false)

// --- Scrolling ---
const scrollerRef = ref<HTMLElement>()
const autoScroll = ref(true)

const { y: scrollTop, arrivedState } = useScroll(scrollerRef, {
    behavior: "smooth"
})

const { height: containerHeight } = useElementSize(scrollerRef)

const ITEM_HEIGHT = 24
const BUFFER_SIZE = 10

const visibleRange = computed(() => {
    const start = Math.floor(scrollTop.value / ITEM_HEIGHT)
    const visibleCount = Math.ceil(containerHeight.value / ITEM_HEIGHT)
    const end = start + visibleCount

    return {
        start: Math.max(0, start - BUFFER_SIZE),
        end: Math.min(searchResults.value.length, end + BUFFER_SIZE)
    }
})

const visibleItems = computed(() => {
    return searchResults.value.slice(visibleRange.value.start, visibleRange.value.end)
})

const totalHeight = computed(() => {
    return searchResults.value.length * ITEM_HEIGHT
})

const offsetY = computed(() => {
    return visibleRange.value.start * ITEM_HEIGHT
})

const scrollToBottom = () => {
    if (scrollerRef.value) {
        scrollerRef.value.scrollTop = scrollerRef.value.scrollHeight
    }
}

// Watch scroll position to detect if at bottom
watch(
    () => arrivedState.bottom,
    (isAtBottom) => {
        autoScroll.value = isAtBottom
    }
)

// --- Frames ---
const addFrame = (frame: Frame) => {
    if (isPaused.value) return

    // Add timestamp to frame
    frame.timestamp = Date.now()

    frames.value.push(frame)

    // Trim old frames to prevent memory issues
    if (frames.value.length > maxFrames) {
        frames.value = frames.value.slice(-maxFrames)
    }

    // Auto-scroll to bottom if enabled
    if (autoScroll.value) {
        nextTick(() => {
            scrollToBottom()
        })
    }
}

const downloadLogs = () => {
    const content = frames.value.map((f) => f.value).join("\n")
    const blob = new Blob([content], { type: "text/plain" })
    const url = URL.createObjectURL(blob)
    const a = document.createElement("a")
    a.href = url
    a.download = `instance-${props.instanceId}-logs.txt`
    a.click()
    URL.revokeObjectURL(url)
}

const clearConsole = () => {
    frames.value = []
    logsEnded.value = false
    scrollTop.value = 0
}

const filterStream = ref<"ALL" | "STDOUT" | "STDERR">("ALL")
const filteredFrames = computed(() => {
    if (filterStream.value === "ALL") return frames.value
    return frames.value.filter((f) => f.stream.name === filterStream.value)
})

// --- Search ---
const searchQuery = ref("")
const searchResults = computed(() => {
    if (!searchQuery.value.trim()) return filteredFrames.value

    const query = searchQuery.value.toLowerCase()
    return filteredFrames.value.filter((f) => f.value.toLowerCase().includes(query))
})

const highlightSearch = (text: string) => {
    const query = searchQuery.value.trim()
    if (!query) return text

    const regex = new RegExp(`(${query.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")})`, "gi")
    return text.replace(regex, "<mark>$1</mark>")
}

// --- WebSocket Listener ---
const isConnected = ref(false)
const logsEnded = ref(false)
const fetechedWhileStopped = ref(false)

let unsubscribeStart: (() => void) | null = null
let unsubscribeFrames: (() => void) | null = null
let unsubscribeEnd: (() => void) | null = null

const handleLogsEnd = () => {
    logsEnded.value = true
    isConnected.value = false
}

const setupListeners = () => {
    unsubscribeStart = websocketService.listen(
        WebSocketOpCodes.InstanceLogsRequestStarted,
        (payload: { running: boolean }) => {
            isConnected.value = true

            console.log("Fetching messages: is server running?", payload.running)
            fetechedWhileStopped.value = !payload.running
        }
    )

    unsubscribeFrames = websocketService.listen(WebSocketOpCodes.InstanceLogsRequestFrame, addFrame)

    unsubscribeEnd = websocketService.listen(WebSocketOpCodes.InstanceLogsRequestFinished, () => {
        if (!fetechedWhileStopped.value) {
            handleLogsEnd()
        }
    })
}

onMounted(() => {
    websocketService.send(WebSocketOpCodes.InstanceLogsRequest, {
        iid: props.instanceId
    })

    setupListeners()
})

onUnmounted(() => {
    unsubscribeStart?.()
    unsubscribeFrames?.()
    unsubscribeEnd?.()
})

// --- Finally ---
watch([searchQuery, filterStream], () => {
    scrollTop.value = 0
    if (scrollerRef.value) {
        scrollerRef.value.scrollTop = 0
    }
})
</script>

<template>
    <div class="console-container">
        <div class="console-toolbar">
            <div class="toolbar-left">
                <span class="frame-count">
                    {{ searchResults.length }} / {{ frames.length }} lines
                </span>
            </div>

            <div class="toolbar-right">
                <div
                    class="connection-status"
                    :class="{
                        disconnected: fetechedWhileStopped || !isConnected,
                        ended: logsEnded
                    }"
                >
                    <span class="status-dot"></span>
                    <template v-if="logsEnded"> Logs Ended </template>
                    <template v-else-if="fetechedWhileStopped"> Server closed </template>
                    <template v-else>
                        {{ isConnected ? "Connected" : "Disconnected" }}
                    </template>
                </div>

        <div class="console-wrapper">
            <div class="fade-overlay fade-top"></div>

            <div v-if="searchResults.length > 0" ref="scrollerRef" class="console-output">
                <div class="virtual-scroller-spacer" :style="{ height: `${totalHeight}px` }">
                    <div
                        class="virtual-scroller-content"
                        :style="{ transform: `translateY(${offsetY}px)` }"
                    >
                        <div
                            v-for="(item, idx) in visibleItems"
                            :key="visibleRange.start + idx"
                            :class="['console-line', `stream-${item.stream.name.toLowerCase()}`]"
                        >
                            <span class="line-number">{{ visibleRange.start + idx + 1 }}</span>
                            <!-- <span class="line-stream">[{{ item.stream.name }}]</span> -->
                            <span class="line-content" v-html="highlightSearch(item.value)"></span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="fade-overlay fade-bottom"></div>

            <div v-if="searchResults.length === 0" class="console-empty">
                <template v-if="logsEnded"> Log stream completed </template>
                <template v-else-if="searchQuery"> No matching logs found... </template>
                <template v-else> Waiting for logs... </template>
            </div>
        </div>
    </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=DM+Mono&display=swap");

.console-container {
    display: flex;
    flex-direction: column;
    height: 100%;
    color: #d4d4d4;
    font-family: "DM Mono", "Consolas", "Monaco", "Courier New", monospace;
    font-size: 14px;
}

.console-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 32px;
    background: transparent;
    border-bottom: 1px solid transparent;
    gap: 12px;
    flex-wrap: wrap;
    flex-shrink: 0;
}

.toolbar-left,
.toolbar-right {
    display: flex;
    gap: 8px;
    align-items: center;
    flex-wrap: wrap;
}

.console-toolbar button,
.console-toolbar select {
    padding: 6px 12px;
    background: #3e3e42;
    border: 1px solid #555;
    color: #d4d4d4;
    border-radius: 4px;
    cursor: pointer;
    font-size: 12px;
    transition: all 0.2s;
}

.console-toolbar button:hover:not(:disabled),
.console-toolbar select:hover {
    background: #505050;
}

.console-toolbar button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.console-toolbar button.active {
    background: #0e639c;
    border-color: #0e639c;
}

@keyframes pulse {
    0%,
    100% {
        opacity: 1;
    }
    50% {
        opacity: 0.7;
    }
}

.search-input {
    padding: 6px 12px;
    background: #3e3e42;
    border: 1px solid #555;
    color: #d4d4d4;
    border-radius: 4px;
    font-size: 12px;
    min-width: 200px;
    transition: border-color 0.2s;
}

.search-input:focus {
    outline: none;
    border-color: #0e639c;
}

.search-input::placeholder {
    color: #858585;
}

.frame-count {
    font-size: 12px;
    color: #858585;
    white-space: nowrap;
}

.connection-status {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 4px 12px;
    background: #2d4a2d;
    border: 1px solid #4a7c4a;
    border-radius: 4px;
    font-size: 12px;
    color: #8cd98c;
    font-weight: 500;
    transition: all 0.3s;
}

.connection-status.disconnected {
    background: #4a2d2d;
    border-color: #7c4a4a;
    color: #f48771;
}

.connection-status.ended {
    background: #3d3d2d;
    border-color: #7c7c4a;
    color: #f9a825;
}

.status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #8cd98c;
    animation: blink 2s infinite;
}

.connection-status.disconnected .status-dot {
    background: #f48771;
}

.connection-status.ended .status-dot {
    background: #f9a825;
    animation: none;
}

@keyframes blink {
    0%,
    100% {
        opacity: 1;
    }
    50% {
        opacity: 0.3;
    }
}

.console-wrapper {
    flex: 1;
    position: relative;
    overflow: hidden;
}

.fade-overlay {
    position: absolute;
    left: 0;
    right: 10px;
    height: 40px;
    pointer-events: none;
    z-index: 10;
    transition: opacity 0.3s;
}

.fade-top {
    top: 0;
    background: linear-gradient(
        to bottom,
        rgb(30, 39, 46) 0%,
        rgba(30, 39, 46, 0.93) 20%,
        rgba(30, 39, 46, 0.8) 40%,
        rgba(30, 39, 46, 0.6) 60%,
        rgba(30, 39, 46, 0.4) 80%,
        transparent 100%
    );
}

.fade-bottom {
    bottom: 0;
    background: linear-gradient(
        to top,
        rgb(30, 39, 46) 0%,
        rgba(30, 39, 46, 0.93) 20%,
        rgba(30, 39, 46, 0.8) 40%,
        rgba(30, 39, 46, 0.6) 60%,
        rgba(30, 39, 46, 0.4) 80%,
        transparent 100%
    );
}

.console-output {
    height: 100%;
    overflow-y: auto;
    overflow-x: hidden;
    position: relative;
    scroll-behavior: smooth;
    scroll-snap-type: y proximity;
}

.virtual-scroller-spacer {
    position: relative;
    width: 100%;
}

.virtual-scroller-content {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    will-change: transform;
}

.console-line {
    display: flex;
    padding: 2px 8px;
    line-height: 1.5;
    white-space: pre-wrap;
    word-break: break-all;
    height: 24px;
    align-items: center;
    scroll-snap-align: start;
}

.console-line:hover {
    background-color: rgba(0, 0, 0, 0.2);
}

.line-number {
    min-width: 50px;
    color: #858585;
    text-align: right;
    padding-right: 12px;
    user-select: none;
    flex-shrink: 0;
}

.line-stream {
    min-width: 80px;
    padding-right: 12px;
    font-weight: bold;
    flex-shrink: 0;
}

.stream-stdout .line-stream {
    color: #4ec9b0;
}

.stream-stderr .line-stream {
    color: #f48771;
}

.line-content {
    flex: 1;
    color: #d4d4d4;
}

.line-content :deep(mark) {
    background: #f9a825;
    color: #000;
    padding: 2px 4px;
    border-radius: 2px;
}

.console-empty {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    color: #858585;
    padding: 40px;
    font-size: 16px;
    pointer-events: none;
    z-index: 5;
}

.console-output::-webkit-scrollbar {
    width: 10px;
}

.console-output::-webkit-scrollbar-track {
    background: #1e1e1e;
}

.console-output::-webkit-scrollbar-thumb {
    background: #424242;
    border-radius: 5px;
}

.console-output::-webkit-scrollbar-thumb:hover {
    background: #4e4e4e;
}

.console-output {
    scrollbar-width: thin;
    scrollbar-color: #424242 #1e1e1e;
}
</style>
