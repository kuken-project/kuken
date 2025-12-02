<template>
  <textarea
    class="textarea"
    @input="onInputChange"
  />
</template>

<script setup lang="ts">
const emits = defineEmits(["update:modelValue"])

function onInputChange(event: Event): void {
    emits('update:modelValue', (event.target as HTMLTextAreaElement).value)
}
</script>
