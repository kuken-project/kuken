<template>
  <div class="stepper__step">
    <slot />
  </div>
</template>
<script setup lang="ts">
defineProps<{
  name: string
}>()
</script>
