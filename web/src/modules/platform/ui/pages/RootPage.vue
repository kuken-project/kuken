<template>
  <component :is="layout" v-if="layout != null">
    <RouterView />
  </component>
  <RouterView v-else />
</template>
<script lang="ts" setup>
import RootLayout from "@/modules/platform/ui/layouts/RootLayout.vue"
import { isUndefined } from "@/utils"
import { computed } from "vue"
import { useRoute } from "vue-router"

const currentRoute = useRoute()
const layout = computed(() =>
  isUndefined(currentRoute.meta.layout) ? RootLayout : currentRoute.meta.layout
)
</script>
