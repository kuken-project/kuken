<template>
  <div
    class="errorState"
    :class="{ 'errorState--alignedY': alignY }"
  >
    <VIcon
      v-if="icon"
      :name="icon"
      class="errorState__icon"
    />
    <div class="errorState__text">
      <slot />
    </div>
  </div>
</template>
<script setup lang="ts">
import VIcon from "@/modules/platform/ui/components/icons/VIcon.vue"

defineProps<{
    icon?: string,
    alignY?: true
}>()
</script>