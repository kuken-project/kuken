<script setup lang="ts">
import type { Instance } from "@/modules/instances/api/models/instance.model.ts"
import instancesService from "@/modules/instances/api/services/instances.service.ts"
import Resource from "@/modules/platform/ui/components/Resource.vue"
import { ref } from "vue"

const props = defineProps<{ instanceId: string }>()
const resource = () => instancesService().getInstance(props.instanceId)
const instance = ref<Instance | null>(null)
</script>

<template>
  <Resource :resource="resource" @loaded="(value) => (instance = value)">
    <template v-if="instance"> Pirocoptero {{ instance.id }} </template>
  </Resource>
</template>

<style scoped lang="scss"></style>
