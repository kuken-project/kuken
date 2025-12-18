<template>
    <Resource :resource="resource" @loaded="(value) => (blueprint = value)">
        <div v-if="blueprint">Blueprint {{ blueprint.spec.name }}</div>
    </Resource>
</template>
<script setup lang="ts">
import { ref } from "vue"
import type { Blueprint } from "@/modules/blueprints/api/models/blueprint.model"
import blueprintsService from "@/modules/blueprints/api/services/blueprints.service"
import Resource from "@/modules/platform/ui/components/Resource.vue"

const props = defineProps<{ blueprintId: string }>()

const resource = () => blueprintsService.getBlueprint(props.blueprintId)
const blueprint = ref<Blueprint | null>(null)
</script>
