<script lang="ts" setup>
import type { BlueprintBuildInput } from "@/modules/blueprints/api/models/blueprint.spec.model.ts"
import VFieldSet from "@/modules/platform/ui/components/form/VFieldSet.vue"
import VInput from "@/modules/platform/ui/components/form/VInput.vue"
import VLabel from "@/modules/platform/ui/components/form/VLabel.vue"

const props = defineProps<Partial<BlueprintBuildInput>>()
const model = defineModel()
</script>

<template>
  <VFieldSet>
    <VLabel>
      {{ props.label }}
      <VInput v-model="model" required="true" type="text" />
    </VLabel>
  </VFieldSet>
</template>
