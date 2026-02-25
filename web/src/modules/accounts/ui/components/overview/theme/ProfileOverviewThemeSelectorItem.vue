<script setup lang="ts">
import ThemeAuto from "@/modules/accounts/ui/components/overview/theme/ThemeAuto.vue"
import ThemeDark from "@/modules/accounts/ui/components/overview/theme/ThemeDark.vue"
import ThemeLight from "@/modules/accounts/ui/components/overview/theme/ThemeLight.vue"
import VIcon from "@/modules/platform/ui/components/icons/VIcon.vue"
import type { BasicColorSchema } from "@vueuse/core"
import { useI18n } from "petite-vue-i18n"

const { t } = useI18n()
defineProps<{ mode: BasicColorSchema; selected: boolean }>()
</script>

<template>
  <div :class="[$style.root, { [$style.selected]: selected }]">
    <div :class="$style.image">
      <ThemeLight v-if="mode === 'light'" />
      <ThemeDark v-else-if="mode === 'dark'" />
      <ThemeAuto v-else-if="mode === 'auto'" />
    </div>
    <div :class="$style.checkbox" v-if="selected">
      <VIcon :class="$style.icon" name="Check" />
    </div>
    <span :class="$style.label">{{ t(`profile.appearence.theme.label.${mode}`) }}</span>
  </div>
</template>

<style module lang="scss">
.root {
  position: relative;
}

.root:not(.selected):hover {
  cursor: pointer;
  .image {
    border-color: var(--kt-border-medium);
  }
}

.selected .image {
  border-color: var(--kt-content-primary);
}

.image {
  display: flex;
  border: 2px solid var(--kt-border-low);
  border-radius: 12px;
  overflow: hidden;

  svg {
    width: 200px;
    display: block;
    border: 2px solid var(--kt-background-surface);
    border-radius: 12px;
  }
}

.label {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-top: 0.4rem;
  gap: 0.8rem;
}

.checkbox {
  border-radius: 50%;
  width: 20px;
  height: 20px;
  position: absolute;
  right: 0;
  top: 0;
  margin: 7px 8px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon {
  position: relative;
  width: 12px;
  height: 12px;
  fill: var(--kt-content-primary);
}
</style>
