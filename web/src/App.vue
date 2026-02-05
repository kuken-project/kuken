<template>
  <LoadingState v-if="isLoading" />
  <RouterView v-else />
  <ModalsContainer />
</template>

<script lang="ts" setup>
import backendService from "@/modules/platform/api/services/backend.service.ts"
import configService from "@/modules/platform/api/services/config.service.ts"
import websocketService from "@/modules/platform/api/services/websocket.service.ts"
import { usePlatformStore } from "@/modules/platform/platform.store.ts"
import LoadingState from "@/modules/platform/ui/components/LoadingState.vue"
import { SETUP_ROUTE } from "@/router.ts"
import { useHead } from "@unhead/vue"
import { useDark } from "@vueuse/core"
import { onUnmounted, ref } from "vue"
import { ModalsContainer } from "vue-final-modal"
import { useRouter } from "vue-router"

const isLoading = ref(true)

useHead({
  title: configService.appName,
  meta: [
    {
      name: "color-scheme",
      content: "light dark"
    }
  ]
})

useDark({
  selector: "body",
  storageKey: "kk-theme",
  attribute: "color-scheme",
  valueDark: "dark"
})

const platformStore = usePlatformStore()
const router = useRouter()

backendService
  .getInfo()
  .catch(console.error)
  .finally(() => {
    if (!platformStore.hasBackendInfo) {
      router.push({ name: SETUP_ROUTE }).finally(() => (isLoading.value = false))
    } else {
      isLoading.value = false
    }
  })

onUnmounted(() => websocketService.close())
</script>
