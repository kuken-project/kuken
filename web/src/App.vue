<template>
    <LoadingState v-if="isLoading" :class="$style.loading" />
    <router-view v-else />
    <ModalsContainer />
</template>

<script lang="ts" setup>
import { ModalsContainer } from "vue-final-modal"
import LoadingState from "@/modules/platform/ui/components/LoadingState.vue"
import { ref } from "vue"
import backendService from "@/modules/platform/api/services/backend.service.ts"
import { useHead } from "@unhead/vue"
import configService from "@/modules/platform/api/services/config.service.ts"
import { SETUP_ROUTE } from "@/router.ts"
import { useRouter } from "vue-router"
import { usePlatformStore } from "@/modules/platform/platform.store.ts"

const isLoading = ref(true)

useHead({
    title: configService.appName
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
</script>
<style lang="scss">
#app {
    display: flex;
    display: -ms-flexbox;
    flex-direction: column;
    height: 100%;
}
</style>
<style lang="scss" module>
.loading {
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
}
</style>
