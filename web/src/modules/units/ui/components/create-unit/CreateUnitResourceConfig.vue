<script lang="ts" setup>
import VCard from "@/modules/platform/ui/components/card/VCard.vue"
import VLabel from "@/modules/platform/ui/components/form/VLabel.vue"
import VSliderInput from "@/modules/platform/ui/components/form/VSliderInput.vue"
import VCol from "@/modules/platform/ui/components/grid/VCol.vue"
import VContainer from "@/modules/platform/ui/components/grid/VContainer.vue"
import VIcon from "@/modules/platform/ui/components/icons/VIcon.vue"
import { useI18n } from "petite-vue-i18n"

const { t } = useI18n()

export type ResourceModel = {
  runtime: string
  memory: number
  cpu: number
  disk: number
  swap: number
}

defineProps<{
  modelValue: ResourceModel
}>()

defineEmits<{
  "update:modelValue": [value: ResourceModel]
}>()
</script>

<template>
  <div :class="$style.rc">
    <div :class="$style.runtime">
      <VLabel>{{ t("units.create.resources.runtime.label") }}</VLabel>
      <div :class="$style.runtimeOptions">
        <div
          :class="[$style.rt, modelValue.runtime === 'docker' && $style.rtSelected]"
          @click="$emit('update:modelValue', { ...modelValue, runtime: 'docker' })"
        >
          <div :class="$style.rtIcon">
            <VIcon name="Docker" />
          </div>
          <div :class="$style.rtContent">
            <span :class="$style.rtName">Docker</span>
            <span :class="$style.rtDesc">{{ t("units.create.resources.runtime.dockerDesc") }}</span>
          </div>
        </div>
      </div>
    </div>

    <div :class="$style.hardware">
      <VLabel>{{ t("units.create.resources.hardware.label") }}</VLabel>
      <VCard :class="$style.hardwareCard">
        <div :class="$style.sliders">
          <VContainer>
            <VCol :size="6">
              <div :class="$style.sliderRow">
                <div :class="$style.sliderHead">
                  <span :class="$style.sliderName">{{
                    t("units.create.resources.hardware.cpu.name")
                  }}</span>
                  <span :class="$style.sliderDesc">{{
                    t("units.create.resources.hardware.cpu.desc")
                  }}</span>
                </div>
                <VSliderInput
                  :model-value="modelValue.cpu"
                  :min="5"
                  :max="400"
                  :step="5"
                  unit="%"
                  @update:model-value="$emit('update:modelValue', { ...modelValue, cpu: $event })"
                />
                <span :class="$style.sliderDetail">{{
                  t("units.create.resources.hardware.cpu.detail")
                }}</span>
              </div>
            </VCol>
            <VCol :size="6">
              <div :class="$style.sliderRow">
                <div :class="$style.sliderHead">
                  <span :class="$style.sliderName">{{
                    t("units.create.resources.hardware.memory.name")
                  }}</span>
                  <span :class="$style.sliderDesc">{{
                    t("units.create.resources.hardware.memory.desc")
                  }}</span>
                </div>
                <VSliderInput
                  :model-value="modelValue.memory"
                  :min="128"
                  :max="16384"
                  :step="128"
                  unit="MB"
                  @update:model-value="
                    $emit('update:modelValue', { ...modelValue, memory: $event })
                  "
                />
                <span :class="$style.sliderDetail">{{
                  t("units.create.resources.hardware.memory.detail")
                }}</span>
              </div>
            </VCol>
          </VContainer>
          <div :class="$style.sliderRow">
            <div :class="$style.sliderHead">
              <span :class="$style.sliderName">{{
                t("units.create.resources.hardware.disk.name")
              }}</span>
              <span :class="$style.sliderDesc">{{
                t("units.create.resources.hardware.disk.desc")
              }}</span>
            </div>
            <VSliderInput
              :model-value="modelValue.disk"
              :min="512"
              :max="65536"
              :step="512"
              unit="MB"
              @update:model-value="$emit('update:modelValue', { ...modelValue, disk: $event })"
            />
            <span :class="$style.sliderDetail">{{
              t("units.create.resources.hardware.disk.detail")
            }}</span>
          </div>
          <div :class="$style.sliderRow">
            <div :class="$style.sliderHead">
              <span :class="$style.sliderName">{{
                t("units.create.resources.hardware.swap.name")
              }}</span>
              <span :class="$style.sliderDesc">{{
                t("units.create.resources.hardware.swap.desc")
              }}</span>
            </div>
            <VSliderInput
              :model-value="modelValue.swap"
              :min="0"
              :max="4096"
              :step="64"
              unit="MB"
              @update:model-value="$emit('update:modelValue', { ...modelValue, swap: $event })"
            />
            <span :class="$style.sliderDetail">{{
              t("units.create.resources.hardware.swap.detail")
            }}</span>
          </div>
        </div>
      </VCard>
    </div>
  </div>
</template>

<style module lang="scss">
.rc {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 3.2rem;
}

.runtime {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.runtimeOptions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rt {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 18px;
  border: 2px solid var(--kt-border-low);
  border-radius: 14px;
  cursor: pointer;
  transition:
    border-color 0.15s ease,
    background-color 0.15s ease;

  &:hover {
    border-color: var(--kt-border-medium);
  }
}

.rtSelected {
  border-color: var(--kt-content-primary);
  background-color: rgba(255, 116, 56, 0.04);

  &:hover {
    border-color: var(--kt-content-primary);
  }

  .rtIcon {
    color: var(--kt-content-primary);
  }
}

.rtIcon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  min-width: 48px;
  border-radius: 12px;
  background-color: var(--kt-background-surface);
  color: var(--kt-content-neutral);
  transition: color 0.15s ease;

  :deep(svg),
  :deep(.icon) {
    width: 26px;
    height: 26px;
  }
}

.rtContent {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.rtName {
  font-weight: 600;
  color: var(--kt-content-neutral-high);
}

.rtDesc {
  color: var(--kt-content-neutral);
  line-height: 1.45;
}

.hardware {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.hardwareCard {
  display: flex;
  flex-direction: column;
}

.sliders {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.sliderRow {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.sliderHead {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.sliderName {
  font-weight: 600;
  color: var(--kt-content-neutral-high);
}

.sliderDesc {
  color: var(--kt-content-neutral);
}

.sliderDetail {
  color: var(--kt-content-neutral-low);
  line-height: 1.45;
  margin-top: 2px;
}
</style>
