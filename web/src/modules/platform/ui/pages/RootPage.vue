<template>
  <component :is="layout">
    <router-view />
  </component>
</template>
<script setup lang="ts">
import { useRouter } from "vue-router"
import RootLayout from "@/modules/platform/ui/layouts/RootLayout.vue"
import { computed } from "vue"

const router = useRouter()
const layout = computed(() => router.currentRoute.value.meta.layout ?? RootLayout)
</script>