<template>
  <div ref="root" class="loading" />
</template>
<script setup lang="ts">
import { onMounted, useTemplateRef } from "vue"
import { useLoading } from "vue-loading-overlay"

const root = useTemplateRef("root")
const loading = useLoading({
  isFullPage: true,
  lockScroll: true
})

onMounted(() => {
  loading.show({ container: root?.value as unknown as HTMLElement })
})
</script>
<style lang="scss" scoped>
.loading {
  position: relative;
  width: 100%;
  height: 100%;
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
