<script lang="ts" setup>
import VLabel from "@/modules/platform/ui/components/form/VLabel.vue"
import VFieldSet from "@/modules/platform/ui/components/form/VFieldSet.vue"
import type { BlueprintBuildInput } from "@/modules/blueprints/api/models/blueprint.model.ts"

const props = defineProps<Partial<BlueprintBuildInput>>()
const model = defineModel()
</script>

<template>
    <VFieldSet>
        <VLabel>
            {{ props.label }}
            <select v-model="model">
                <option disabled value="">Please select one</option>
                <option v-for="option in props.items" :key="option" :value="option">
                    {{ option }}
                </option>
            </select>
            <p>Selected Value: {{ modelValue }}</p>
        </VLabel>
    </VFieldSet>
</template>

<style lang="scss" scoped></style>
