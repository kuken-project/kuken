<script setup lang="ts">
import {ref} from "vue";
import instancesService from "@/modules/instances/api/services/instances.service.ts";
import type {Instance} from "@/modules/instances/api/models/instance.model.ts";
import Resource from "@/modules/platform/ui/components/Resource.vue";

const props = defineProps<{ instanceId: string }>()
const resource = () => instancesService().getInstance(props.instanceId)
const instance = ref<Instance | null>(null)
</script>

<template>
  <Resource :resource="resource" @loaded="(value) => (instance = value)">
    <template v-if="instance">
      <router-view :instance="instance" />
    </template>
  </Resource>
</template>

<style scoped lang="scss">

</style>