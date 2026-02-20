<template>
  <LoadingState v-if="state.isLoading" key="loading" />
  <EmptyState v-else-if="state.isEmpty" key="empty">
    <slot name="empty" />
    <VButton v-if="includeRefreshButton" :class="$style.button" @click="load"> Refresh </VButton>
  </EmptyState>
  <template v-else-if="error && error.code === 2002">
    <!-- Do nothing -->
  </template>
  <slot v-else-if="state.prohibited" key="prohibited" name="prohibited">
    <div class="prohibited">{{ t("resource.nopermission") }}</div>
  </slot>
  <slot v-else key="content" :refresh="load" />
</template>

<script lang="ts" setup>
import logService from "@/modules/platform/api/services/log.service"
import EmptyState from "@/modules/platform/ui/components/EmptyState.vue"
import LoadingState from "@/modules/platform/ui/components/LoadingState.vue"
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import { useI18n } from "petite-vue-i18n"
import { reactive, ref } from "vue"
import { useRouter } from "vue-router"

const { t } = useI18n()
const emits = defineEmits(["loaded", "error"])
const props = defineProps({
  resource: { required: true, type: Function<Promise<unknown>> },
  includeRefreshButton: { type: Boolean, required: false, default: true },
  redirectNotAllowed: { type: Boolean, required: false, default: false }
})
const state = reactive({
  isLoading: true,
  isEmpty: false,
  prohibited: false
})
const error = ref<Error | null>(null)

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

function onDataLoaded(value: unknown) {
  // TODO Check empty state
  emits("loaded", value)
}

const router = useRouter()
function onError(errorArg: Error) {
  if (errorArg.code && errorArg.code === 2002 /* Insufficient permissions */) {
    if (props.redirectNotAllowed) router.replace({ name: "not-allowed" })
    else state.prohibited = true

    return
  }

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
<style lang="scss" scoped>
.prohibited {
  color: var(--kt-content-negative);
  background-color: var(--kt-content-negative-overlay);
  text-align: center;
  border-radius: 12px;
  padding: 16px;
}
</style>
