<script lang="ts" setup>
import type { Instance } from "@/modules/instances/api/models/instance.model.ts"
import instancesService from "@/modules/instances/api/services/instances.service.ts"
import { useInstanceStore } from "@/modules/instances/instances.store.ts"
import Resource from "@/modules/platform/ui/components/Resource.vue"
import { computed, onUnmounted } from "vue"

defineProps<{ instanceId: string }>()

const instanceStore = useInstanceStore()
const instance = computed(() => instanceStore.instance)

function onInstanceLoaded(instance: Instance) {
  instanceStore.updateInstance(instance)
}

onUnmounted(() => {
  instanceStore.$reset()
  instanceStore.$dispose()
})
</script>

<template>
  <Resource :resource="() => instancesService.getInstance(instanceId)" @loaded="onInstanceLoaded">
    <template v-if="instance">
      <router-view :instanceId="instance.id" />
    </template>
  </Resource>
</template>

<style lang="scss" scoped></style>
