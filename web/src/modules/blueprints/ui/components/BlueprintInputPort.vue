<script lang="ts" setup>
import VInput from "@/modules/platform/ui/components/form/VInput.vue"
import VLabel from "@/modules/platform/ui/components/form/VLabel.vue"
import VFieldSet from "@/modules/platform/ui/components/form/VFieldSet.vue"
import { onMounted } from "vue"
import type { BlueprintBuildInput } from "@/modules/blueprints/api/models/blueprint.model.ts"

const props = defineProps<Partial<BlueprintBuildInput>>()
const model = defineModel()

onMounted(() => {
    model.value = props.default
})
</script>

<template>
    <VFieldSet>
        <VLabel>
            {{ props.label }}
            <VInput v-model="model" required="true" type="number" />
        </VLabel>
    </VFieldSet>
</template>

<style lang="scss" scoped></style>
