<script lang="ts" setup>
const props = defineProps<{ value: string }>()
</script>

<template>
    <span>{{ props.value }}</span>
</template>
