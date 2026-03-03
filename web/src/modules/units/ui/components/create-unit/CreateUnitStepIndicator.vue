<template>
  <div class="stepper">
    <div v-for="(step, index) in steps" :key="index" class="stepper__step">
      <div class="stepper__rail">
        <div class="stepper__circle">
          <svg v-if="step.icon === 'tag'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z" />
            <line x1="7" y1="7" x2="7.01" y2="7" />
          </svg>
          <svg v-else-if="step.icon === 'blueprint'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="7" height="7" />
            <rect x="14" y="3" width="7" height="7" />
            <rect x="14" y="14" width="7" height="7" />
            <rect x="3" y="14" width="7" height="7" />
          </svg>
          <svg v-else-if="step.icon === 'resources'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
          </svg>
          <svg v-else-if="step.icon === 'configure'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="3" />
            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
          </svg>
          <span v-else>{{ index + 1 }}</span>
        </div>
        <div v-if="index < steps.length - 1" class="stepper__line" />
      </div>
      <div class="stepper__content">
        <div class="stepper__heading">
          <span class="stepper__label">{{ step.label }}</span>
          <span class="stepper__number">Step {{ index + 1 }} of {{ steps.length }}</span>
        </div>
        <p v-if="step.description" class="stepper__description">{{ step.description }}</p>
        <div class="stepper__body">
          <slot :name="`step-${index}`" />
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
defineProps<{
  steps: { label: string; description?: string; icon?: string }[]
}>()
</script>

<style lang="scss" scoped>
.stepper {
  display: flex;
  flex-direction: column;

  &__step {
    display: flex;
    flex-direction: row;
    gap: 2.4rem;
  }

  &__rail {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex-shrink: 0;
    padding-top: 2px;
  }

  &__circle {
    width: 42px;
    height: 42px;
    border-radius: 12px;
    border: 2px solid var(--kt-content-primary);
    background: rgba(255, 116, 56, 0.06);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 600;
    color: var(--kt-content-primary);
    flex-shrink: 0;
  }

  &__line {
    width: 2px;
    flex: 1;
    background-color: var(--kt-border-medium);
    margin: 8px 0;
    border-radius: 1px;
  }

  &__content {
    flex: 1;
    min-width: 0;
    padding-bottom: 4rem;
  }

  &__heading {
    display: flex;
    align-items: baseline;
    gap: 14px;
    margin-bottom: 6px;
    line-height: 42px;
  }

  &__label {
    font-family: var(--kt-headline-font), serif;
    font-size: 20px;
    font-weight: 700;
    color: var(--kt-content-neutral-high);
  }

  &__number {
    font-size: 13px;
    font-weight: 500;
    color: var(--kt-content-neutral-low);
  }

  &__description {
    font-size: 14.5px;
    color: var(--kt-content-neutral);
    line-height: 1.55;
    margin: 0 0 2rem;
  }

  &__body {
    display: flex;
    flex-direction: column;
  }
}
</style>
