<template>
    <LoadingState v-if="state.isLoading" key="loading" />
    <EmptyState v-else-if="state.isEmpty" key="empty">
        <slot name="empty" />
        <VButton v-if="includeRefreshButton" :class="$style.button" @click="load">
            empty-state.refresh
        </VButton>
    </EmptyState>
    <slot v-else key="content" :refresh="load" />
</template>

<script setup lang="ts">
import EmptyState from "@/modules/platform/ui/components/EmptyState.vue"
import LoadingState from "@/modules/platform/ui/components/LoadingState.vue"
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import { reactive, ref } from "vue"
import logService from "@/modules/platform/api/services/log.service"

const emits = defineEmits(["loaded", "error"])
const props = defineProps<{
    resource: () => Promise<unknown>
    includeRefreshButton?: true
}>()
const state = reactive({
    isLoading: true,
    isEmpty: false
})
const error = ref<Error | null>(null)

// Functions
function load(): void {
    error.value = null
    state.isEmpty = false
    state.isLoading = true

    props
        .resource()
        .then(onDataLoaded)
        .catch(onError)
        .finally(() => (state.isLoading = false))
}

// Functions
function onDataLoaded(value: unknown) {
    // TODO Check empty state
    emits("loaded", value)
}

function onError(errorArg: Error) {
    logService.error("Failed to load resource", errorArg)
    error.value = errorArg
    emits("error", errorArg)
}

load()
</script>
<style lang="scss" module>
.empty__body {
    margin-top: 1.6rem;
}

.button:not(:last-child) {
    margin-right: 0.8rem;
}
</style>
