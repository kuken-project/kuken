<template>
  <label
    class="label" 
    :class="{
      'label--onColor': onColor,
      'label--hight': high,
      'label--required': required
    }"
  >
    <slot />
  </label>
</template>

<script setup lang="ts">
defineProps<{
  onColor?: boolean
  high?: boolean
  required?: boolean
}>()
</script>