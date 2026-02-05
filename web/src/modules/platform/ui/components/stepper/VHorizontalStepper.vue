<template>
  <div class="stepper" @update-step="onCurrentStepChange">
    <p>Current step: {{ currentStep }}</p>
    <ul v-if="steps.length > 0" class="stepper__step-list">
      <li v-for="(_step, index) in steps" :key="index" @click="onCurrentStepChange">
        Pirocoptero @ {{ index }}
        <!--				{{ step.$props.name }}-->
      </li>
    </ul>
    <template v-for="(_step, index) in steps" :key="index">
      <keep-alive>
        <component :is="activeStep" v-if="currentStep === index" />
      </keep-alive>
    </template>
  </div>
</template>
<script setup lang="ts">
import { undefinedToNull } from "@/utils"
import { onMounted, reactive, ref, useSlots, watch } from "vue"

const props = defineProps<{ currentStep: number }>()
const slots = useSlots()

// State
const children: any[] = slots.default!()
const maxSteps = ref(children.length)
const activeStep = ref(children[props.currentStep])
const steps = reactive(children)

// Effects
onMounted(() => {
  console.log("child", children)
})

watch([props.currentStep], (value) => {
  const nextIndex = Math.min(value[0]!, maxSteps.value)
  activeStep.value = undefinedToNull(steps[nextIndex])
})

// Functions
function onCurrentStepChange(): void {}
</script>
