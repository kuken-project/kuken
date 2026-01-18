<script lang="ts" setup>
import blueprintsService from "@/modules/blueprints/api/services/blueprints.service.ts"
import Resource from "@/modules/platform/ui/components/Resource.vue"
import { ref } from "vue"
import type { Blueprint } from "@/modules/blueprints/api/models/blueprint.model.ts"
import BlueprintInputPort from "@/modules/blueprints/ui/components/BlueprintInputPort.vue"
import BlueprintInputText from "@/modules/blueprints/ui/components/BlueprintInputText.vue"
import VCard from "@/modules/platform/ui/components/card/VCard.vue"
import BlueprintInputDataSize from "@/modules/blueprints/ui/components/BlueprintInputDataSize.vue"
import BlueprintInputPassword from "@/modules/blueprints/ui/components/BlueprintInputPassword.vue"
import BlueprintInputSelect from "@/modules/blueprints/ui/components/BlueprintInputSelect.vue"
import BlueprintInputCheckbox from "@/modules/blueprints/ui/components/BlueprintInputCheckbox.vue"
import VContainer from "@/modules/platform/ui/components/grid/VContainer.vue"
import VCol from "@/modules/platform/ui/components/grid/VCol.vue"

const props = defineProps<{ blueprintId: string }>()
const blueprint = ref<Blueprint>()

const model = defineModel<{ [name: string]: string }>({ default: {} })

function updateInput(name: string, value: any): void {
    model.value[name] = value.toString()
}
</script>

<template>
    <Resource
        :resource="() => blueprintsService.getBlueprint(props.blueprintId)"
        @loaded="(result: Blueprint) => (blueprint = result)"
    >
        <template v-if="blueprint">
            <VContainer>
                <VCol :size="6">
                    <h4>Options</h4>
                    <p class="description">Modify {{ blueprint.spec.metadata.name }} settings</p>
                    <VCard>
                        <template v-for="input in blueprint.spec.inputs" :key="input.name">
                            <BlueprintInputText
                                v-if="input.type == 'text'"
                                :label="input.label"
                                :modelValue="model[input.name]"
                                :sensitive="input.sensitive"
                                @update:modelValue="
                                    (value) => updateInput(input.name as string, value)
                                "
                            />
                            <BlueprintInputPort
                                v-if="input.type == 'port'"
                                :default="input.default?.value"
                                :label="input.label"
                                :modelValue="model[input.name]"
                                :sensitive="input.sensitive"
                                @update:modelValue="
                                    (value) => updateInput(input.name as string, value)
                                "
                            />
                            <BlueprintInputDataSize
                                v-if="input.type == 'datasize'"
                                :default="input.default?.value"
                                :label="input.label"
                                :modelValue="model[input.name]"
                                :sensitive="input.sensitive"
                                @update:modelValue="
                                    (value) => updateInput(input.name as string, value)
                                "
                            />
                            <BlueprintInputPassword
                                v-if="input.type == 'password'"
                                :default="input.default?.value"
                                :label="input.label"
                                :modelValue="model[input.name]"
                                :sensitive="input.sensitive"
                                @update:modelValue="
                                    (value) => updateInput(input.name as string, value)
                                "
                            />
                            <BlueprintInputSelect
                                v-if="input.type == 'select'"
                                :default="input.default?.value"
                                :items="input.items"
                                :label="input.label"
                                :modelValue="model[input.name]"
                                :sensitive="input.sensitive"
                                @update:modelValue="
                                    (value) => updateInput(input.name as string, value)
                                "
                            />
                            <BlueprintInputCheckbox
                                v-if="input.type == 'checkbox'"
                                :default="input.default?.value"
                                :label="input.label"
                                :modelValue="model[input.name]"
                                :sensitive="input.sensitive"
                                @update:modelValue="
                                    (value) => updateInput(input.name as string, value)
                                "
                            />
                        </template>
                    </VCard>
                </VCol>
                <VCol :size="6">
                    <h4>Startup Parameters</h4>
                    <VCard>
                        {{ blueprint.spec.instanceSettings?.startup }}
                    </VCard>
                </VCol>
            </VContainer>
        </template>
    </Resource>
</template>

<style lang="scss" scoped>
.description {
    color: var(--kt-content-neutral);
}

.card {
    margin-top: 0.8rem;
}
</style>
