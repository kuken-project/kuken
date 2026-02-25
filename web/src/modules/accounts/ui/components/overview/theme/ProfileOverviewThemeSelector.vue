<script setup lang="ts">
import ProfileOverviewThemeSelectorItem from "@/modules/accounts/ui/components/overview/theme/ProfileOverviewThemeSelectorItem.vue"
import VLayout from "@/modules/platform/ui/components/grid/VLayout.vue"
import VSection from "@/modules/platform/ui/components/typography/VSection.vue"
import { type BasicColorSchema, useColorMode } from "@vueuse/core"
import { useI18n } from "petite-vue-i18n"

const { t } = useI18n()
const colorMode = useColorMode({
  emitAuto: true,
  selector: "body",
  storageKey: "kk-theme",
  attribute: "color-scheme"
})
</script>

<template>
  <VSection>
    <template #title>{{ t("profile.appearence.title") }}</template>
    <template #description>{{ t("profile.appearence.description") }}</template>
    <VLayout direction="horizontal" gap="sm">
      <ProfileOverviewThemeSelectorItem
        v-for="theme in ['light', 'dark', 'auto'] as BasicColorSchema[]"
        :key="theme"
        :mode="theme"
        :selected="colorMode === theme"
        @click="colorMode = theme"
      />
    </VLayout>
  </VSection>
</template>
