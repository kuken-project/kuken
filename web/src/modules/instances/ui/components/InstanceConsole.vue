<script lang="ts" setup>
import { useConsoleFrames } from "@/modules/instances/composables/useConsoleFrames.ts"
import { useConsoleLoader } from "@/modules/instances/composables/useConsoleLoader.ts"
import { computed, nextTick, onMounted, onUnmounted, ref, useTemplateRef, watch } from "vue"
import type { Frame } from "@/modules/instances/api/models/frame.model.ts"
import { useConsoleWebSocket } from "@/modules/instances/composables/useConsoleWebSocket.ts"
import ConsoleLoadingIndicator from "@/modules/instances/ui/components/console/ConsoleLoadingIndicator.vue"
import ConsoleEndIndicator from "@/modules/instances/ui/components/console/ConsoleEndIndicator.vue"
import ConsoleEmpty from "@/modules/instances/ui/components/console/ConsoleEmpty.vue"
import { useScroll } from "@vueuse/core"
import { DynamicScroller, DynamicScrollerItem } from "vue-virtual-scroller"
import ConsoleLine from "@/modules/instances/ui/components/console/ConsoleLine.vue"
import instancesService from "@/modules/instances/api/services/instances.service.ts"

const props = defineProps<{
    instanceId: string
    anchorId?: string
}>()

const LOAD_THRESHOLD = 200
const LOAD_BATCH_SIZE = 100
const MAX_FRAMES = 200

// =================================================
// COMPOSABLES
// =================================================

const {
    frames,
    oldestSeqId,
    newestSeqId,
    addFrame,
    preprendFrames,
    appendFrames,
    setFrames,
    trimToRecent,
    findByPersistentId
} = useConsoleFrames({ maxFrames: MAX_FRAMES })

const {
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
} = useConsoleLoader({
    instanceId: props.instanceId,
    batchSize: LOAD_BATCH_SIZE
})

// =================================================
// STATE
// =================================================

const scrollerRef = useTemplateRef("scrollerRef")
const { y, arrivedState } = useScroll(scrollerRef, {
    offset: {
        top: LOAD_THRESHOLD,
        bottom: LOAD_THRESHOLD
    }
})

// Para fade (offset pequeno)
const { arrivedState: fadeState } = useScroll(scrollerRef, {
    offset: { top: 20, bottom: 20 }
})

const autoScroll = ref(true)
const showJumpButton = computed(() => hasNewerLogs.value || !autoScroll.value)

// =================================================
// SCROLL
// =================================================
watch(
    () => arrivedState.top,
    (atTop) => {
        console.log("atTop", atTop)
        console.log("hasOlderLogs", hasOlderLogs.value)
        if (atTop && hasOlderLogs.value) {
            handleLoadOlder()
        }
    }
)

watch(
    () => arrivedState.bottom,
    (atBottom) => {
        console.log("atBottom", atBottom)
        if (atBottom) {
            console.log("hasNewerLogs", hasNewerLogs.value)
            if (hasNewerLogs.value) {
                handleLoadNewer()
            } else {
                autoScroll.value = true
            }
        } else {
            autoScroll.value = false
        }
    }
)

function scrollToBottom() {
    nextTick(() => {
        if (scrollerRef.value) {
            scrollerRef.value.scrollTop = scrollerRef.value.scrollHeight
        }
    })
}

// =================================================
// WEBSOCKET
// =================================================

function handleNewFrame(frame: Frame) {
    const { trimmedOld } = addFrame(frame)
    if (trimmedOld) {
        setHasOlderLogs(true)
    }

    if (autoScroll.value) {
        nextTick(() => {})
    }
}

const {
    isConnected,
    logsEnded,
    subscribe,
    unsubscribe,
    reconnect: wsReconnect
} = useConsoleWebSocket({
    instanceId: props.instanceId,
    onFrame: handleNewFrame
})

// =================================================
// LOADING
// =================================================

async function handleLoadOlder() {
    const oldestTimestamp = frames.value[0]?.timestamp
    if (!oldestTimestamp) return

    const newFrames = await loadOlderLogs(oldestTimestamp)
    if (newFrames.length > 0) {
        const { trimmedNew } = preprendFrames(newFrames)
        if (trimmedNew) setHasNewerLogs(true)
    }
}

async function handleLoadNewer() {
    const newestTimestamp = frames.value[frames.value.length - 1]?.timestamp
    if (!newestTimestamp) return

    const newFrames = await loadNewerLogs(newestTimestamp)
    if (newFrames.length > 0) {
        const { trimmedOld } = appendFrames(newFrames)
        if (trimmedOld) setHasOlderLogs(true)
    }

    if (!hasNewerLogs.value) {
        autoScroll.value = true
    }
}

async function navigateToAnchor(persistentId: string) {
    const timestamp = parseInt(persistentId.split("-")[0]!, 10)
    const newFrames = await loadAroundTimestamp(timestamp)

    setFrames(newFrames)
    autoScroll.value = false

    await nextTick()
    const index = findByPersistentId(persistentId)
    if (index !== -1) {
        // TODO Scroll to item
        highlightFrame(persistentId)
    }
}

function highlightFrame(persistentId: string) {
    setTimeout(() => {
        const element = document.querySelector(`[data-persistent-id="${persistentId}"]`)
        if (element) {
            element.classList.add("highlighted")
            setTimeout(() => element.classList.remove("highlighted"), 3000)
        }
    }, 100)
}

// =================================================
// ACTIONS
// =================================================

function jumpToPresent() {
    resetToRealtime()
    autoScroll.value = false
    trimToRecent(LOAD_BATCH_SIZE)
    setHasOlderLogs(true)
    scrollToBottom()
}

function reconnect() {
    resetToRealtime()
    autoScroll.value = true
    wsReconnect()
}

function copyAnchorLink(frame: Frame) {
    const url = `${window.location.origin}/instances/${props.instanceId}/console#${frame.persistentId}`
    navigator.clipboard.writeText(url)
}

// =================================================
// LIFECYCLE
// =================================================

onMounted(async () => {
    if (props.anchorId) {
        await navigateToAnchor(props.anchorId)
    }

    const { frames, hasMore } = await instancesService.getLogs(props.instanceId, {
        limit: MAX_FRAMES
    })
    if (frames.length > 0) {
        setFrames(frames)
        hasOlderLogs.value = hasMore
        console.log(`Filling console up with ${frames.length} frames`)
    }

    const lastFrameTimestamp = frames[frames.length - 1]?.timestamp ?? 0
    subscribe(lastFrameTimestamp)

    scrollToBottom()
})

onUnmounted(() => unsubscribe("unmounted"))
</script>

<template>
    <div :class="{ hidden: fadeState.top }" class="fade-top"></div>
    <div class="console-container">
        <ConsoleLoadingIndicator v-if="isLoadingOlder" position="top" />
        <ConsoleEndIndicator v-else-if="!hasOlderLogs && frames.length > 0" position="top" />

        <DynamicScroller
            ref="scrollerRef"
            :items="frames"
            :min-item-size="24"
            class="console-output"
            key-field="seqId"
        >
            <template #default="{ item, index, active }">
                <DynamicScrollerItem :active="active" :data-index="index" :item="item">
                    <ConsoleLine :frame="item" @copy-link="copyAnchorLink" />
                </DynamicScrollerItem>
            </template>
        </DynamicScroller>

        <ConsoleLoadingIndicator v-if="isLoadingNewer" position="bottom" />
        <ConsoleEndIndicator
            v-else-if="hasNewerLogs"
            clickable
            position="bottom"
            @click="jumpToPresent"
        />

        <ConsoleEmpty v-if="frames.length === 0 && !isLoadingOlder" />
    </div>
    <div :class="{ hidden: fadeState.bottom }" class="fade-bottom"></div>
</template>

<style lang="scss" scoped>
@import url("https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;700&display=swap");

.console-container {
    display: flex;
    flex-direction: column;
    height: 100%;
    color: #d4d4d4;
    font-family: "JetBrains Mono", monospace;
}

.console-wrapper {
    flex: 1;
    position: relative;
    overflow: hidden;
}

.console-output :deep(.vue-recycle-scroller__item-wrapper) {
    scroll-snap-type: y proximity;
}

.console-output {
    height: 100%;
    overflow-y: auto;
    overflow-x: hidden;
    position: relative;
    scroll-behavior: smooth;
    scrollbar-width: thin;
    scrollbar-color: #424242 #1e1e1e;

    &::-webkit-scrollbar {
        width: 10px;
    }

    &::-webkit-scrollbar-track {
        background: #1e1e1e;
    }

    &::-webkit-scrollbar-thumb {
        background: #424242;
        border-radius: 5px;
    }

    &::-webkit-scrollbar-thumb:hover {
        background: #4e4e4e;
    }
}

.console-output::before,
.console-output::after {
    content: "";
    position: sticky;
    left: 0;
    right: 0;
    height: 40px;
    pointer-events: none;
    z-index: 10;
}

.console-output::before {
    top: 0;
    background: linear-gradient(to bottom, #0d0d0d, transparent);
}

.console-output::after {
    bottom: 0;
    background: linear-gradient(to top, #0d0d0d, transparent);
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

.fade-top,
.fade-bottom {
    position: absolute;
    left: 0;
    right: 0;
    height: 60px;
    pointer-events: none;
    z-index: 10;
}

.fade-top {
    top: 0;
    background: linear-gradient(to bottom, rgba(30, 39, 46, 0.8) 0%, rgba(30, 39, 46, 0) 100%);
    box-shadow: 0 4px 20px rgba(30, 39, 46, 0.4);
}

.fade-bottom {
    bottom: 0;
    background: linear-gradient(to top, rgba(30, 39, 46, 0.8) 0%, rgba(30, 39, 46, 0) 100%);
    box-shadow: 0 -4px 20px rgba(30, 39, 46, 0.4);
}

.fade-top,
.fade-bottom {
    transition: opacity 0.4s ease;
}

.fade-top.hidden,
.fade-bottom.hidden {
    opacity: 0;
}
</style>
