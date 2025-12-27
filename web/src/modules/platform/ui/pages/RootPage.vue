<template>
    <component :is="layout">
        <router-view />
    </component>
</template>
<script setup lang="ts">
import { useRoute } from "vue-router"
import RootLayout from "@/modules/platform/ui/layouts/RootLayout.vue"
import { computed } from "vue"

const currentRoute = useRoute()
const layout = computed(() => currentRoute.meta.layout ?? RootLayout)
</script>
