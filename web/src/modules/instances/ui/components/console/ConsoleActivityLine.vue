<script lang="ts" setup>
import type { ActivityFrame } from "@/modules/instances/api/models/frame.model.ts"
import dayjs from "dayjs"
import { computed } from "vue"

const props = defineProps<{
  frame: ActivityFrame
  text: string
}>()

const formattedTime = computed(() => dayjs(props.frame.ts).format("dddd, MMMM D, YYYY h:mm A"))
</script>

<template>
  <div class="console-line">
    <span class="line-number">{{ props.frame.seqId }}</span>
    <span :title="formattedTime" class="line-content" v-html="props.frame.msg" />
  </div>
</template>

<style lang="scss" scoped>
.console-line {
  display: flex;
  align-items: center;
  box-sizing: border-box;
  padding: 2px 8px;
  min-height: 24px;
  height: auto;
  scroll-snap-align: start;
}

.console-line:hover {
  background-color: rgba(0, 0, 0, 0.2);
}

.line-number {
  min-width: 50px;
  color: rgba(255, 255, 255, 0.18);
  text-align: right;
  padding-right: 12px;
  user-select: none;
  flex-shrink: 0;
}

.line-content {
  flex: 1;
  color: #cbd5e1;
  white-space: pre-wrap;
  word-break: break-all;
}

.line-content :deep(mark) {
  background-color: #fbbf24;
  color: #1f2937;
  border-radius: 2px;
  padding: 1px 2px;
}

.copy-link {
  opacity: 0;
  padding: 0.4rem 0.8rem;
  margin-left: 0.8rem;
  font-family: inherit;
  font-size: 12px;
  color: #505050;
  background-color: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15 ease;

  &:hover {
    color: #909090;
    background: #262626;
  }
}
</style>
