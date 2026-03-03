<script lang="ts" setup>
import type {
  InterpolatedBlueprintProperty,
  ResolveBlueprintResponse
} from "@/modules/blueprints/api/models/blueprint.spec.model.ts"
import blueprintsService from "@/modules/blueprints/api/services/blueprints.service.ts"
import BlueprintInputCheckbox from "@/modules/blueprints/ui/components/BlueprintInputCheckbox.vue"
import BlueprintInputDataSize from "@/modules/blueprints/ui/components/BlueprintInputDataSize.vue"
import BlueprintInputPassword from "@/modules/blueprints/ui/components/BlueprintInputPassword.vue"
import BlueprintInputPort from "@/modules/blueprints/ui/components/BlueprintInputPort.vue"
import BlueprintInputSelect from "@/modules/blueprints/ui/components/BlueprintInputSelect.vue"
import BlueprintInputText from "@/modules/blueprints/ui/components/BlueprintInputText.vue"
import VCard from "@/modules/platform/ui/components/card/VCard.vue"
import VFieldSet from "@/modules/platform/ui/components/form/VFieldSet.vue"
import VInput from "@/modules/platform/ui/components/form/VInput.vue"
import VLabel from "@/modules/platform/ui/components/form/VLabel.vue"
import VCol from "@/modules/platform/ui/components/grid/VCol.vue"
import Resource from "@/modules/platform/ui/components/Resource.vue"
import { isUndefined } from "@/utils"
import { useI18n } from "petite-vue-i18n"
import { ref } from "vue"

const { t } = useI18n()

const props = defineProps<{ blueprintId: string }>()
const resolution = ref<ResolveBlueprintResponse>()
const startup = ref<InterpolatedBlueprintProperty>()

const model = defineModel<{ [name: string]: string }>({ default: {} })

function updateInput(name: string, value: unknown & {}): void {
  if (isUndefined(value)) return
  model.value[name] = value.toString()
}

function onSpecLoaded(result: ResolveBlueprintResponse) {
  resolution.value = result
  startup.value = resolution.value.startup as InterpolatedBlueprintProperty
}
</script>

<template>
  <Resource
    :resource="() => blueprintsService.resolveBlueprint(props.blueprintId)"
    @loaded="onSpecLoaded"
  >
    <template v-if="resolution">
      <div :class="$style.configure">
        <div :class="$style.network">
          <VCol :size="8">
            <VFieldSet>
              <VLabel>{{ t("units.create.configure.host") }}</VLabel>
              <VInput v-model="model['network.host']" autofocus type="text" placeholder="0.0.0.0" />
            </VFieldSet>
          </VCol>
          <VCol :size="4">
            <VFieldSet>
              <VLabel>{{ t("units.create.configure.port") }}</VLabel>
              <VInput v-model="model['network.port']" required="true" type="number" />
            </VFieldSet>
          </VCol>
        </div>

        <VCard v-if="resolution.inputs && resolution.inputs.length > 0" :class="$style.inputs">
          <template v-for="input in resolution.inputs" :key="input.name">
            <BlueprintInputText
              v-if="input.type == 'text'"
              :model-value="model[input.name]"
              v-bind="input"
              @update:model-value="(value: string) => updateInput(input.name as string, value)"
            />
            <BlueprintInputPort
              v-if="input.type == 'port'"
              :model-value="model[input.name]"
              v-bind="input"
              @update:model-value="(value: string) => updateInput(input.name as string, value)"
            />
            <BlueprintInputDataSize
              v-if="input.type == 'datasize'"
              :model-value="model[input.name]"
              v-bind="input"
              @update:model-value="(value: string) => updateInput(input.name as string, value)"
            />
            <BlueprintInputPassword
              v-if="input.type == 'password'"
              :model-value="model[input.name]"
              v-bind="input"
              @update:model-value="(value: string) => updateInput(input.name as string, value)"
            />
            <BlueprintInputSelect
              v-if="input.type == 'select'"
              :model-value="model[input.name]"
              v-bind="input"
              @update:model-value="(value: string) => updateInput(input.name as string, value)"
            />
            <BlueprintInputCheckbox
              v-if="input.type == 'checkbox'"
              :model-value="Boolean(model[input.name] ?? false)"
              v-bind="input"
              @update:model-value="
                (value: boolean) => updateInput(input.name as string, value.toString())
              "
            />
          </template>
        </VCard>
      </div>
    </template>
  </Resource>
</template>

<style module lang="scss">
.configure {
  display: flex;
  flex-direction: column;
  gap: 1.6rem;
}

.network {
  display: flex;
  flex-direction: row;
}

.inputs {
  :deep(fieldset:not(:last-child)) {
    margin-bottom: 3.2rem;
  }
}
</style>
