<template>
  <div
    class="button"
    :class="{
      'card--hoverable': hoverable,
      'card--flat': flat,
      'card--reduced': reduced,
      'card--onSurface': onSurface,
      'card--outlined': outlined,
      'card--scrollable': scrollable
    }"
  >
    <div
      v-if="$slots.title"
      class="card__title"
    >
      <slot name="title" />
    </div>
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  hoverable?: boolean
  flat?: boolean
  reduced?: boolean
  onSurface?: boolean
  outlined?: boolean
  scrollable?: boolean
}>()
</script>
