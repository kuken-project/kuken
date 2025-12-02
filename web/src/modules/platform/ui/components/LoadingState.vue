<template>
  <div
    ref="root"
    class="loading-state"
  />
</template>
<script setup lang="ts">
import { onMounted, ref } from "vue"
import { useLoading } from "vue-loading-overlay"

const root = ref(null)
const loading = useLoading({
    isFullPage: true,
    lockScroll: true
})

onMounted(() => {
    loading.show({ container: root?.value as unknown as HTMLElement })
})
</script>