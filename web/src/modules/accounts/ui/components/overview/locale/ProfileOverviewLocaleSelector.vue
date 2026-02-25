<script setup lang="ts">
import VOption from "@/modules/platform/ui/components/form/VOption.vue"
import VSelect from "@/modules/platform/ui/components/form/VSelect.vue"
import VSection from "@/modules/platform/ui/components/typography/VSection.vue"
import { useI18n } from "petite-vue-i18n"
import { onMounted } from "vue"

const model = defineModel({ type: String }),
  i18n = useI18n(),
  availableLocales = i18n.availableLocales.value,
  { t } = i18n

onMounted(() => {
  model.value = i18n.locale.value
})
</script>

<template>
  <VSection>
    <template #title>{{ t("profile.language.title") }}</template>
    <template #description>{{ t("profile.language.description") }}</template>
    <VSelect v-model="model" :class="$style.selector">
      <VOption v-for="locale in availableLocales" :key="locale" :value="t(`locale.${locale}`)">
        {{ t(`locale.${locale}`) }}
      </VOption>
    </VSelect>
  </VSection>
</template>

<style module lang="scss">
.selector {
  max-width: 240px;
}
</style>
