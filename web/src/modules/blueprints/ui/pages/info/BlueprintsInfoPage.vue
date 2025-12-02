<template>
  <Resource
    :resource="resource"
    :reactivity-key="blueprint"
    @loaded="(value: unknown) => blueprint = value"
    @error="onError"
  >
    <div v-if="blueprint">
      Blueprint {{ blueprint.spec.name }}
    </div>
    <div v-else-if="errors.notFound">
      Não encontrado
    </div>
    <ErrorState v-else />
  </Resource>
</template>
<script setup lang="ts">
import { inject, ref } from "vue"
import type { Blueprint } from "@/modules/blueprints/api/models/blueprint.model"
import blueprintsService from "@/modules/blueprints/api/services/blueprints.service"
import Resource from "@/modules/platform/ui/components/Resource.vue"
import ErrorState from "@/modules/platform/ui/components/ErrorState.vue"
import { HttpError, HttpErrors } from "@/modules/platform/api/models/error.model"

const props = defineProps<{ blueprintId: string }>()
defineEmits(["refresh"])

// State
const resource = () => blueprintsService.getBlueprint(props.blueprintId)
const blueprint: Blueprint = ref(null)
const errors = ref({ notFound: false })

// Functions
function onError(error: Error): void {
    errors.value.notFound = error instanceof HttpError && (error as HttpError).code === HttpErrors.UNKNOWN_BLUEPRINT
}
</script>