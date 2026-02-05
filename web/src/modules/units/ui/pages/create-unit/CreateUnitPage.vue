<script lang="ts" setup>
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import VForm from "@/modules/platform/ui/components/form/VForm.vue"
import VContainer from "@/modules/platform/ui/components/grid/VContainer.vue"
import VTitle from "@/modules/platform/ui/components/typography/VTitle.vue"
import type { Unit } from "@/modules/units/api/models/unit.model.ts"
import unitsService from "@/modules/units/api/services/units.service.ts"
import CreateUnitBlueprintSelector from "@/modules/units/ui/components/create-unit/CreateUnitBlueprintSelector.vue"
import CreateUnitConfigureBlueprint from "@/modules/units/ui/components/create-unit/CreateUnitConfigureBlueprint.vue"
import CreateUnitNameInput from "@/modules/units/ui/components/create-unit/CreateUnitNameInput.vue"
import { isUndefined } from "@/utils"
import { useHead } from "@unhead/vue"
import { useAsyncState } from "@vueuse/core"
import { computed, reactive } from "vue"
import { onBeforeRouteLeave, useRouter } from "vue-router"

useHead({
  title: "Create new server"
})

const props = defineProps<{ blueprint?: string }>()

const form = reactive({
  name: "",
  blueprint: props.blueprint ?? "",
  inputs: {},
  env: {}
})

const router = useRouter()

enum Steps {
  Name,
  SelectBlueprint,
  ConfigureBlueprint
}

const steps = reactive({
  current: Steps.Name,
  all: [Steps.Name, Steps.SelectBlueprint, Steps.ConfigureBlueprint]
})

const firstStep = computed(() => steps.all[0])
const lastStep = computed(() => steps.all[steps.all.length - 1])

const goNextButtonLabel = computed(() => {
  return steps.current === lastStep.value ? "Create" : "Next"
})
const goBackButtonLabel = computed(() => {
  return steps.current === firstStep.value ? "Cancel" : "Back"
})

function goBack() {
  const currentIndex = steps.all.indexOf(steps.current)
  if (currentIndex === 0) {
    router.back()
    return
  }

  steps.current = steps.all[currentIndex - 1]!
}

function goNext() {
  const next = steps.all[steps.all.indexOf(steps.current) + 1]!
  if (next === Steps.SelectBlueprint && !isUndefined(props.blueprint)) {
    steps.current = next
    return goNext()
  }

  steps.current = next
}

onBeforeRouteLeave((_, __, next) => {
  if (steps.current !== Steps.Name) {
    goBack()
    return
  }

  next()
})

const { isLoading, execute } = useAsyncState(unitsService.createUnit, null as unknown as Unit, {
  immediate: false,
  onSuccess: (payload: Unit) => {
    window.location.href = router.resolve({
      name: "instance.console",
      params: { instanceId: payload.instance.id, unitId: payload.id }
    }).href
  }
})

const canProceed = computed(() => {
  if (isLoading.value) return false

  if (steps.current == Steps.SelectBlueprint) {
    return form.blueprint.length > 0
  }

  return true
})

function proceed() {
  if (steps.current === lastStep.value) {
    execute(0, form)
  } else {
    goNext()
  }
}
</script>

<template>
  <VContainer class="container">
    <VTitle :centered="true"> Create new server </VTitle>
    <VForm @submit.prevent="proceed">
      <div class="content">
        <CreateUnitNameInput
          v-if="steps.current == Steps.Name"
          :key="Steps.Name"
          v-model="form.name"
        />
        <CreateUnitBlueprintSelector
          v-else-if="steps.current == Steps.SelectBlueprint"
          :key="Steps.SelectBlueprint"
          v-model="form.blueprint"
        />
        <CreateUnitConfigureBlueprint
          v-else-if="steps.current == Steps.ConfigureBlueprint"
          :key="Steps.ConfigureBlueprint"
          v-model="form.inputs"
          :blueprint-id="form.blueprint"
        />
        <div class="buttons">
          <VButton v-if="goBackButtonLabel" class="back" variant="default" @click="goBack">
            {{ goBackButtonLabel }}
          </VButton>
          <VButton :disabled="!canProceed" class="next" type="submit" variant="primary">{{
            goNextButtonLabel
          }}</VButton>
        </div>
      </div>
    </VForm>
  </VContainer>
</template>

<style lang="scss" scoped>
.container {
  padding: 48px;
  display: flex;
  flex-direction: column;
}

.content {
  display: flex;
  flex-direction: column;
  min-width: 50%;
  width: 50%;
  position: relative;
  left: 50%;
  transform: translateX(-50%);
}

.buttons {
  display: flex;
  flex-direction: row;
}

.button {
  margin-top: 3.2rem;
  width: 15%;

  &.back {
    margin-right: auto;
  }
  &.next {
    margin-left: auto;
  }
}
</style>
