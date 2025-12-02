<template>
  <div
    :class="`alert--${variant}`"
    class="alert"
    role="alert"
  >
    <div class="alert__icon">
      <component
        :is="iconName"
        class="alert__icon__svg"
      />
    </div>
    <div class="alert__header">
      <div
        v-if="$slots.title"
        class="alert__title"
      >
        <slot name="title">
          Title
        </slot>
      </div>
      <div
        v-if="$slots.description"
        class="alert__description"
      >
        <slot name="description">
          Description
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed} from "vue"

const props = defineProps<{
  variant: "default" | "info" | "error" | "warn"
}>()

// eslint-disable-next-line vue/return-in-computed-property
const iconName = computed<string>(() => {
    switch (props.variant) {
    case "default":
        return "HelpCircleOutline"
    case "info":
        return "InformationOutline"
    case "error":
        return "AlertCircleOutline"
    case "warn":
        return "AlertOutline"
    }
})
</script>
