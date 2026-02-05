<script lang="ts" setup>
import VIcon from "@/modules/platform/ui/components/icons/VIcon.vue"
import { computed } from "vue"

const props = defineProps<{
  windowStart: number | null
  windowHours: number
}>()

const emit = defineEmits<{
  previous: []
  next: []
  realtime: []
  changeWindow: [hours: number]
}>()

const isRealtime = computed(() => props.windowStart === null)

const windowLabel = computed(() => {
  const hours = props.windowHours
  const end = props.windowStart ? props.windowStart + hours * 60 * 60 * 1000 : Date.now()
  const start = end - hours * 60 * 60 * 1000

  const formatDate = (ts: number) => {
    return new Date(ts).toLocaleString(undefined, {
      day: "2-digit",
      month: "2-digit",
      hour: "2-digit",
      minute: "2-digit"
    })
  }

  return `${formatDate(start)}–${formatDate(end)}`
})
</script>

<template>
  <div class="console-time-nav">
    <button title="Previous window" @click="emit('previous')">
      <VIcon name="AngleLeft" />
    </button>

    <span class="window-label">{{ windowLabel }}</span>

    <button :disabled="isRealtime" title="Next" @click="emit('next')">
      <VIcon name="AngleRight" />
    </button>

    <button :class="{ active: isRealtime }" title="Real time" @click="emit('realtime')">
      Live
    </button>

    <select
      :value="windowHours"
      @change="emit('changeWindow', Number(($event.target as HTMLSelectElement).value))"
    >
      <option :value="1">1 hour</option>
      <option :value="6">6 hours</option>
      <option :value="24">24 hours</option>
      <option :value="72">3 days</option>
    </select>
  </div>
</template>
<style lang="scss" scoped>
.console-time-nav {
  padding: 6px 12px;
  background: rgba(0, 0, 0, 0.12);
  font-family: "JetBrains Mono", "Consolas", "Monaco", "Courier New", monospace;
  color: #d1d5db;
  border-radius: 8px;
  min-width: 240px;
  transition: border-color 0.2s;
  height: 38px;

  button {
    background: none;
    fill: rgba(255, 255, 255, 0.38);

    &:hover {
      background: rgba(255, 255, 255, 0.08);
      cursor: pointer;

      border-radius: 4px;
    }
  }

  .icon {
    max-width: 24px;
    max-height: 24px;
  }
}
</style>
