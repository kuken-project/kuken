<template>
  <div
    class="layout"
    :style="style"
  >
    <slot />
  </div>
</template>
<script setup lang="ts">
import type { CSSProperties } from "vue"

type Gap = "none" | "xs" | "sm" | "md" | "lg"
type Props = {
  direction: "vertical" | "horizontal"
  gap?: Gap
}

const props = withDefaults(defineProps<Props>(), {
    direction: "vertical",
    gap: "none"
})
const gapMappings: { [size: string]: number } = {
    "none": 0,
    "xs": 0.4,
    "sm": 0.8,
    "md": 1.6,
    "lg": 2.4
}
const style: CSSProperties = {
    'flex-direction': props.direction === 'vertical' ? 'column' : 'row',
    'gap': `${gapMappings[props.gap]}rem`
}
</script>