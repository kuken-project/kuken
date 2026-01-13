<template>
    <input
        :class="{
            'input--onSurface': onSurface
        }"
        :disabled="disabled"
        :placeholder="placeholder"
        :readonly="disabled"
        class="input"
        @input="onInputChange"
    />
</template>

<script lang="ts" setup>
import { onMounted, ref } from "vue"

const emits = defineEmits(["update:modelValue"])
const props = defineProps<{
    disabled?: boolean
    placeholder?: string
    onSurface?: boolean
    autoFocus?: boolean
    modelValue?: string
}>()

function onInputChange(event: Event): void {
    emits("update:modelValue", (event.target as HTMLInputElement).value)
}

const rootElement = ref<HTMLInputElement | null>(null)
onMounted(() => {
    if (props.autoFocus) {
        rootElement.value?.focus()
    }
})
</script>
