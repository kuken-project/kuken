<script lang="ts" setup>
import PageWrapper from "@/modules/platform/ui/components/PageWrapper.vue"
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import VForm from "@/modules/platform/ui/components/form/VForm.vue"
import VContainer from "@/modules/platform/ui/components/grid/VContainer.vue"
import VLayout from "@/modules/platform/ui/components/grid/VLayout.vue"
import VIcon from "@/modules/platform/ui/components/icons/VIcon.vue"
import VTitle from "@/modules/platform/ui/components/typography/VTitle.vue"
import type { Unit } from "@/modules/units/api/models/unit.model.ts"
import unitsService from "@/modules/units/api/services/units.service.ts"
import CreateUnitBlueprintSelector from "@/modules/units/ui/components/create-unit/CreateUnitBlueprintSelector.vue"
import CreateUnitConfigureBlueprint from "@/modules/units/ui/components/create-unit/CreateUnitConfigureBlueprint.vue"
import CreateUnitNameInput from "@/modules/units/ui/components/create-unit/CreateUnitNameInput.vue"
import type { ResourceModel } from "@/modules/units/ui/components/create-unit/CreateUnitResourceConfig.vue"
import CreateUnitResourceConfig from "@/modules/units/ui/components/create-unit/CreateUnitResourceConfig.vue"
import { useHead } from "@unhead/vue"
import { useAsyncState } from "@vueuse/core"
import { useI18n } from "petite-vue-i18n"
import { computed, reactive, ref } from "vue"
import { useRouter } from "vue-router"

const { t } = useI18n()

useHead({
  title: t("units.create.docTitle")
})

const props = defineProps<{ blueprint?: string }>()

const form = reactive({
  name: "",
  blueprint: props.blueprint ?? "",
  inputs: {},
  env: {},
  runtime: "docker",
  memory: 1024,
  cpu: 100,
  disk: 10240,
  swap: 256
})

const resourcesOpen = ref(false)

const router = useRouter()

const resourceModel = computed<ResourceModel>({
  get: () => ({
    runtime: form.runtime,
    memory: form.memory,
    cpu: form.cpu,
    disk: form.disk,
    swap: form.swap
  }),
  set: (value: ResourceModel) => {
    form.runtime = value.runtime
    form.memory = value.memory
    form.cpu = value.cpu
    form.disk = value.disk
    form.swap = value.swap
  }
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

const canCreate = computed(() => {
  if (isLoading.value) return false
  if (form.name.length === 0) return false
  if (form.blueprint.length === 0) return false
  return true
})

function submit() {
  execute(0, form)
}
</script>

<template>
  <PageWrapper>
    <VContainer :class="$style.container">
      <VTitle>{{ t("units.create.pageTitle") }}</VTitle>

      <VForm :class="$style.setup" @submit.prevent="submit">
        <section :class="$style.section">
          <div :class="$style.side">
            <div :class="$style.heading">
              <span :class="$style.number">1</span>
              <h4>{{ t("units.create.general.title") }}</h4>
            </div>
            <p>{{ t("units.create.general.description") }}</p>
          </div>
          <div :class="$style.main">
            <CreateUnitNameInput v-model="form.name" />
          </div>
        </section>

        <section :class="$style.section">
          <div :class="$style.side">
            <div :class="$style.heading">
              <span :class="$style.number">2</span>
              <h4>{{ t("units.create.blueprint.title") }}</h4>
            </div>
            <p>{{ t("units.create.blueprint.description") }}</p>
          </div>
          <div :class="$style.main">
            <CreateUnitBlueprintSelector v-model="form.blueprint" />
          </div>
        </section>

        <section :class="$style.section">
          <div :class="$style.side">
            <div :class="$style.heading">
              <span :class="$style.number">3</span>
              <h4>{{ t("units.create.configuration.title") }}</h4>
            </div>
            <p>{{ t("units.create.configuration.description") }}</p>
          </div>
          <div :class="$style.main">
            <CreateUnitConfigureBlueprint
              v-if="form.blueprint"
              v-model="form.inputs"
              :blueprint-id="form.blueprint"
            />
            <div v-else :class="$style.placeholder">
              <VIcon :class="$style.placeholderIcon" name="GridFour" />
              <span>{{ t("units.create.configuration.placeholder") }}</span>
            </div>
          </div>
        </section>

        <section :class="[$style.section, $style.sectionResources]">
          <div :class="$style.resourcesHeader" @click="resourcesOpen = !resourcesOpen">
            <div :class="$style.heading">
              <span :class="$style.number">4</span>
              <div>
                <h4>
                  <VIcon
                    :class="[$style.chevron, resourcesOpen && $style.chevronOpen]"
                    name="ChevronRight"
                  />
                  {{ t("units.create.resources.title") }}
                </h4>
                <p>{{ t("units.create.resources.description") }}</p>
              </div>
            </div>
            <div :class="$style.collapseSummary">
              <VIcon v-if="form.runtime === 'docker'" name="Docker" />
              <span>Docker</span>
              <span :class="$style.collapseDot" />
              <span>{{ form.memory }} MB RAM</span>
              <span :class="$style.collapseDot" />
              <span>{{ form.cpu }}% CPU</span>
            </div>
          </div>
          <div v-if="resourcesOpen" :class="$style.resourcesBody">
            <CreateUnitResourceConfig v-model="resourceModel" />
          </div>
        </section>
      </VForm>

      <footer :class="$style.footer">
        <span>{{ form.blueprint }} {{ form.runtime }}</span>
        <VLayout gap="xs" direction="horizontal">
          <VButton variant="default" @click="router.back()">
            {{ t("units.create.cancel") }}
          </VButton>
          <VButton :disabled="!canCreate" type="submit" variant="primary" @click="submit">
            {{ t("units.create.submit") }}
          </VButton>
        </VLayout>
      </footer>
    </VContainer>
  </PageWrapper>
</template>

<style module lang="scss">
.container {
  padding-bottom: 80px;
}

.setup {
  display: flex;
  flex-direction: column;

  section:not(:first-child) {
    margin-top: 4.8rem;
  }
}

.section {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 4.8rem;
  padding: 2.4rem 0;
  border-bottom: 1px solid var(--kt-border-low);
}

.sectionResources {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.side {
  margin-right: 20%;

  h4 {
    font-family: var(--kt-headline-font), serif;
    margin-bottom: 0.6rem;
  }

  p {
    color: var(--kt-content-neutral);
    line-height: 1.55;
    margin: 0;
  }
}

.heading {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 0.6rem;

  h4 {
    margin-bottom: 0;
  }

  > p {
    margin: 0;
  }

  > div {
    p {
      color: var(--kt-content-neutral);
      line-height: 1.55;
      margin: 0;
      margin-top: 0.4rem;
    }
  }
}

.number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  min-width: 26px;
  border-radius: 8px;
  background-color: var(--kt-content-primary);
  color: #fff;
  font-weight: 700;
  margin-top: 1px;
  position: relative;
  top: 6px;
}

.main {
  min-width: 0;
}

.placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 3.2rem 2rem;
  border: 2px dashed var(--kt-border-low);
  border-radius: 16px;
  color: var(--kt-content-neutral-low);
  text-align: center;
  line-height: 1.5;
}

.placeholderIcon {
  opacity: 0.4;
  width: 40px;
  height: 40px;
}

.resourcesHeader {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
  gap: 2rem;

  h4 {
    display: flex;
    align-items: center;
    gap: 6px;
    font-family: var(--kt-headline-font), serif;
    margin-bottom: 0;
  }
}

.resourcesBody {
  padding-top: 2.4rem;
}

.chevron {
  width: 16px;
  height: 16px;
  color: var(--kt-content-neutral-low);
  flex-shrink: 0;
  transition:
    transform 0.2s ease,
    color 0.2s ease;

  .resourcesHeader:hover & {
    color: var(--kt-content-neutral-high);
  }
}

.chevronOpen {
  transform: rotate(90deg);
}

.collapseSummary {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: var(--kt-content-neutral);
  white-space: nowrap;
  flex-shrink: 0;
}

.collapseDot {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background-color: var(--kt-content-neutral-low);
  flex-shrink: 0;
}

.footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 4rem;
  background-color: var(--kt-background-body);
  border-top: 1px solid var(--kt-border-medium);
  margin: 16px;
  border-bottom-left-radius: 16px;
  border-bottom-right-radius: 16px;
}
</style>
