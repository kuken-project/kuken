<template>
  <LoadingState v-if="isLoading" />
  <RouterView v-else />
  <ModalsContainer />
</template>

<script lang="ts" setup>
import backendService from "@/modules/platform/api/services/backend.service.ts"
import configService from "@/modules/platform/api/services/config.service.ts"
import websocketService from "@/modules/platform/api/services/websocket.service.ts"
import LoadingState from "@/modules/platform/ui/components/LoadingState.vue"
import { useHead } from "@unhead/vue"
import { useDark } from "@vueuse/core"
import { onMounted, onUnmounted, ref } from "vue"
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

const router = useRouter()

onMounted(() => {
  backendService.syncBackendInfo().then((syncSucceeded: boolean) => {
    const isInSetupPage = router.currentRoute.value.name === "setup"

    if (syncSucceeded || isInSetupPage) {
      isLoading.value = false
      return
    }

    // Postpone `isLoading` state until the backend sync is done to ensure current page,
    // which will thrown an error because backend info is not available
    // do not get rendered between first app ticks, backend sync and redirect to the setup page
    router.replace({ name: "setup" }).finally(() => (isLoading.value = false))
  })
})

onUnmounted(() => websocketService.close())
</script>
