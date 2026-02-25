<script lang="ts" setup>
import configService from "@/modules/platform/api/services/config.service.ts"
import VCol from "@/modules/platform/ui/components/grid/VCol.vue"
import VTitle from "@/modules/platform/ui/components/typography/VTitle.vue"
import RootLayout from "@/modules/platform/ui/layouts/RootLayout.vue"
import { SetupStepType } from "@/modules/setup/api/models/setup.model.ts"
import setupService from "@/modules/setup/api/services/setup.service.ts"
import SetupCreateAccount from "@/modules/setup/ui/components/SetupCreateAccount.vue"
import SetupOrganizationName from "@/modules/setup/ui/components/SetupOrganizationName.vue"
import SetupStep from "@/modules/setup/ui/components/SetupStep.vue"
import router from "@/router.ts"
import { useHead } from "@unhead/vue"
import { onMounted, type Reactive, reactive } from "vue"

useHead({
  title: `Configure ${configService.appName} organization`
})

type PendingSetup = {
  currentStep: string
  payload: {
    email: string
    password: string
    orgName: string
  }
}

const remainingSteps: string[] = reactive([])
const setup: Reactive<PendingSetup> = reactive({
  currentStep: "",
  payload: {
    email: "",
    password: "",
    orgName: ""
  }
})

async function completeSetup() {
  await setupService.completeSetup({
    organizationName: setup.payload.orgName,
    account: {
      email: setup.payload.email,
      password: setup.payload.password
    }
  })

  window.location.href = router.resolve({ name: "login" }).href
}

async function proceedSetup() {
  const current = setup.currentStep
  remainingSteps.splice(remainingSteps.indexOf(current), 1)

  if (remainingSteps.length === 0) {
    return await completeSetup()
  }

  setup.currentStep = remainingSteps[0] as string
}

type SetupStepInterface = {
  title: string
  description: string
  stepType: SetupStepType
}

const steps: Array<SetupStepInterface> = [
  {
    title: "Set up your organization",
    description:
      "Choose a name to identify your organization inside the platform.<br/>You can change this later if needed.",
    stepType: SetupStepType.ORGANIZATION_NAME
  },
  {
    title: "Create admin account",
    description:
      "This will be the first account in the system.<br/>You'll use it to access the dashboard, create servers, and manage your settings.",
    stepType: SetupStepType.CREATE_ACCOUNT
  }
]

onMounted(async () => {
  const remoteSetup = await setupService.getSetup()
  if (remoteSetup.completed) return router.push("/")

  for (const step of remoteSetup.remainingSteps) {
    remainingSteps.push(step.type)
  }

  setup.currentStep = remainingSteps[0]!
})
</script>
<template>
  <RootLayout>
    <div :class="$style.content">
      <VTitle>Let’s get started</VTitle>

      <VCol :size="6">
        <div :class="$style.stepsList">
          <SetupStep
            v-for="(step, index) in steps"
            :key="`step-${index}`"
            :active="setup.currentStep == step.stepType"
            :completed="!remainingSteps.includes(step.stepType)"
          >
            <template #icon>
              {{ index + 1 }}
            </template>
            <template #title>
              {{ step.title }}
            </template>
            <template #description>
              <!-- eslint-disable-next-line vue/no-v-html -->
              <span v-html="step.description" />
            </template>
          </SetupStep>
        </div>
      </VCol>
      <VCol :size="6">
        <div :class="$style.stepData">
          <template v-if="setup.currentStep == SetupStepType.CREATE_ACCOUNT">
            <SetupCreateAccount
              v-model:email="setup.payload.email"
              v-model:password="setup.payload.password"
              @done="proceedSetup"
            />
          </template>
          <template v-if="setup.currentStep == SetupStepType.ORGANIZATION_NAME">
            <SetupOrganizationName
              v-model:organization-name="setup.payload.orgName"
              @done="proceedSetup()"
            />
          </template>
        </div>
      </VCol>
    </div>
  </RootLayout>
</template>
<style lang="scss" module>
.content {
  padding: 48px;
}

.stepsList {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.stepData {
  border: 4px dashed #ff8f5f;
  border-radius: 20px;
  padding: 36px;
  height: 60dvh;
}
</style>
