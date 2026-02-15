<script lang="ts" setup>
import type { Frame } from "@/modules/instances/api/models/frame.model.ts"
import instancesService from "@/modules/instances/api/services/instances.service.ts"
import { useAnsiText } from "@/modules/instances/composables/useAnsiText.ts"
import { useConsoleFrames } from "@/modules/instances/composables/useConsoleFrames.ts"
import { useConsoleLoader } from "@/modules/instances/composables/useConsoleLoader.ts"
import { useConsoleWebSocket } from "@/modules/instances/composables/useConsoleWebSocket.ts"
import ConsoleActivityLine from "@/modules/instances/ui/components/console/ConsoleActivityLine.vue"
import ConsoleTextLine from "@/modules/instances/ui/components/console/ConsoleTextLine.vue"
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import VForm from "@/modules/platform/ui/components/form/VForm.vue"
import VInput from "@/modules/platform/ui/components/form/VInput.vue"
import { isUndefined } from "@/utils"
import { useScroll } from "@vueuse/core"
import { computed, nextTick, onMounted, onUnmounted, ref, unref, useTemplateRef, watch } from "vue"
import { DynamicScroller, DynamicScrollerItem } from "vue-virtual-scroller"

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

const { isRealtime, loadLogs, goToPrevious, goToNext, goToRealtime, goToDate } = useConsoleLoader({
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

const { arrivedState: fadeState } = useScroll(scrollerRef, {
  offset: { top: 20, bottom: 20 }
})

const autoScroll = ref(true)

function scrollToBottom() {
  nextTick(() => {
    const scroller = scrollerRef.value?.$el
    if (!scroller) return

    let lastScrollTop = -1
    const tryScroll = () => {
      scroller.scrollTop = scroller.scrollHeight

      if (scroller.scrollTop !== lastScrollTop) {
        lastScrollTop = scroller.scrollTop
        requestAnimationFrame(tryScroll)
      }
    }

    tryScroll()
  })
}

// =================================================
// WEBSOCKET
// =================================================

function handleNewFrame(frame: Frame) {
  addFrame(frame)

  if (autoScroll.value) {
    scrollToBottom()
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

function copyAnchorLink(frame: Frame) {
  const url = `${window.location.origin}/instances/${props.instanceId}/console#${frame.persistentId}`
  navigator.clipboard.writeText(url)
}

// =================================================
// LIFECYCLE
// =================================================

onMounted(async () => {
  if (props.anchorId) {
    // TODO navigateToAnchor
  }

  const { frames, hasMore } = await instancesService.getLogs(props.instanceId, {})
  if (frames.length > 0) {
    setFrames(frames)
    console.log(`Filling console up with ${frames.length} frames`)
  }

  const lastFrameTimestamp = frames[frames.length - 1]?.timestamp ?? 0
  subscribe(lastFrameTimestamp)

  setTimeout(() => scrollToBottom(), 500)
})

onUnmounted(() => unsubscribe("unmounted"))

watch(isRealtime, (realtime) => {
  if (realtime) {
    subscribe(0)
  } else {
    unsubscribe("realtime")
  }
})

// const windowHours = ref(6) // Padrão: 6 horas
// const windowStart = ref<number | null>(null) // Padrão: tempo real
//
// async function handlePrevious() {
//     goToPrevious()
//     const newFrames = await loadLogs()
//     setFrames(newFrames)
// }
//
// async function handleNext() {
//     goToNext()
//     const newFrames = await loadLogs()
//     setFrames(newFrames)
// }
//
// async function handleRealtime() {
//     goToRealtime()
//     const newFrames = await loadLogs()
//     setFrames(newFrames)
// }
//
// async function handleChangeWindow(hours: number) {
//     windowHours.value = hours
//     const newFrames = await loadLogs()
//     setFrames(newFrames)
// }

const command = ref("")
const commandInput = useTemplateRef("commandInput")
const history = ref<string[]>([])
const historyIndex = ref(-1)

async function sendCommand() {
  const input = unref(command.value)
  command.value = ""
  history.value.push(input)

  const { exitCode } = await instancesService.runInstanceCommand(props.instanceId, input)

  console.log(`Exit code for ${input}: ${exitCode}`)
}

function setCommandToLastInHistory(direction: "up" | "down") {
  const mod = direction === "up" ? -1 : 1

  if (historyIndex.value == -1) historyIndex.value = history.value.length + mod
  else historyIndex.value = historyIndex.value + mod

  const next = history.value[historyIndex.value]
  if (isUndefined(next)) historyIndex.value = -1
  else {
    command.value = next
    setTimeout(() => {
      const input: HTMLInputElement = commandInput.value!.$el
      input.setSelectionRange(next.length, next.length)
    }, 1)
  }
}

const searchQuery = ref("")
const searchResults = computed(() => {
  let result: Frame[]
  if (!searchQuery.value.trim()) result = frames.value
  else {
    const query = searchQuery.value.toLowerCase()
    result = frames.value.filter((f) => f.msg.toLowerCase().includes(query))
  }

  return result.map((f) => {
    return { ...f, value: highlightSearch(useAnsiText(f.msg)) }
  })
})

const highlightSearch = (text: string) => {
  const query = searchQuery.value.trim()
  if (!query) return text

  const regex = new RegExp(`(${query.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")})`, "gi")
  return text.replace(regex, "<mark>$1</mark>")
}
</script>

<template>
  <div class="console-container">
    <div class="console-toolbar">
      <div class="toolbar-left">
        <VButton variant="default" @click="$router.back">Go back</VButton>
        <input v-model="searchQuery" class="search-input" placeholder="Search..." type="text" />
        <span class="frame-count"> {{ searchResults.length }} / {{ frames.length }} lines </span>
      </div>

      <div class="toolbar-right">
        <div class="connection-status">
          <span class="status-dot"></span>
          Connected
        </div>
        <!-- <ConsoleTimeNavigator
                    :window-hours="windowHours"
                    :window-start="windowStart"
                    @next="handleNext"
                    @previous="handlePrevious"
                    @realtime="handleRealtime"
                    @change-window="handleChangeWindow"
                /> -->
      </div>
    </div>

    <div class="console-wrapper">
      <div :class="{ hidden: fadeState.top }" class="fade-top" />

      <DynamicScroller
        ref="scrollerRef"
        :items="searchResults"
        :min-item-size="24"
        class="console-output"
        key-field="seqId"
      >
        <template #default="{ item, index, active }">
          <DynamicScrollerItem :active="active" :data-index="index" :item="item">
            <ConsoleTextLine
              v-if="item.type === 'console'"
              :key="item.seqId"
              :frame="item"
              :text="item.value"
              @copy-link="copyAnchorLink"
            />
            <ConsoleActivityLine
              v-if="item.type === 'activity'"
              :key="item.seqId"
              :frame="item"
              :text="item.msg"
            />
          </DynamicScrollerItem>
        </template>
      </DynamicScroller>

      <div :class="{ hidden: fadeState.bottom }" class="fade-bottom" />
    </div>

    <VForm class="command" @submit.prevent="sendCommand">
      <VInput
        ref="commandInput"
        v-model="command"
        auto-focus
        placeholder="Type something..."
        @keydown.up.stop="setCommandToLastInHistory('up')"
        @keydown.down.stop="setCommandToLastInHistory('down')"
      />
    </VForm>
  </div>
</template>

<style lang="scss" scoped>
.console-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  color: #d4d4d4;
  font-family: "JetBrains Mono", monospace;
  scroll-snap-type: y proximity;
}

.console-wrapper {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.console-output :deep(.vue-recycle-scroller__item-wrapper) {
  scroll-snap-type: y mandatory;
}

.console-output {
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  scroll-snap-type: y proximity;
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

  .toolbar-left,
  .toolbar-right {
    display: flex;
    gap: 8px;
    align-items: center;
    flex-wrap: wrap;
  }

  button {
    background-color: rgba(0, 0, 0, 0.12);
    height: 38px;

    &:hover {
      background-color: rgba(0, 0, 0, 0.18) !important;
    }
  }

  .search-input {
    padding: 6px 12px;
    background: rgba(0, 0, 0, 0.12);
    font-family: "JetBrains Mono", "Consolas", "Monaco", "Courier New", monospace;
    color: #d1d5db;
    border-radius: 8px;
    min-width: 240px;
    transition: border-color 0.2s;
    height: 38px;
  }

  .search-input:focus {
    outline: none;
    border-color: #0e639c;
  }

  .frame-count {
    white-space: nowrap;
    margin-left: 12px;
    color: #d1d5db;
  }
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  background: #2d4a2d;
  border: 1px solid #4a7c4a;
  border-radius: 8px;
  font-size: 12px;
  color: #8cd98c;
  font-weight: 500;
  transition: all 0.3s;

  &.disconnected {
    background: #4a2d2d;
    border-color: #7c4a4a;
    color: #f48771;

    .status-dot {
      background: #f48771;
    }
  }

  &.ended {
    background: #3d3d2d;
    border-color: #7c7c4a;
    color: #f9a825;

    .status-dot {
      background: #f9a825;
      animation: none;
    }
  }

  .status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #8cd98c;
    animation: blink 2s infinite;
  }
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

.command input {
  padding: 6px 12px;
  background: rgba(0, 0, 0, 0.12);
  font-family: "JetBrains Mono", "Consolas", "Monaco", "Courier New", monospace;
  color: #d1d5db;
  border-radius: 0;
  transition: border-color 0.2s;
  height: 48px;

  &::placeholder {
    font-family: "JetBrains Mono", "Consolas", "Monaco", "Courier New", monospace;
    font-style: normal;
    color: inherit;
  }
}
</style>
