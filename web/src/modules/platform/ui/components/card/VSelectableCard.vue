<template>
  <VCard
    :hoverable="true"
    :active="selected"
    :disabled="disabled"
    :aria-disabled="disabled"
    class="card--selectable"
    @click="onSelect"
  >
    <div class="card--selectable__radio" @click="onSelect" />
    <div class="card--selectable__content">
      <div class="card--selectable__title">
        <slot name="title" />
      </div>
      <div v-if="$slots.description" class="card--selectable__description">
        <slot name="description" />
      </div>
    </div>
  </VCard>
</template>
<script setup lang="ts">
import VCard from "@/modules/platform/ui/components/card/VCard.vue"

defineProps<{
  selected: boolean
  disabled?: boolean
}>()

const emit = defineEmits<{
  select: []
}>()

function onSelect(): void {
  emit("select")
}
</script>
