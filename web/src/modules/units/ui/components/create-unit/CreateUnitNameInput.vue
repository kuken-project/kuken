<script lang="ts" setup>
import VFieldSet from "@/modules/platform/ui/components/form/VFieldSet.vue"
import VInput from "@/modules/platform/ui/components/form/VInput.vue"
import VLabel from "@/modules/platform/ui/components/form/VLabel.vue"
import { useI18n } from "petite-vue-i18n"

const { t } = useI18n()
const model = defineModel<string>({ required: true })
</script>

<template>
  <VFieldSet>
    <VLabel>{{ t("units.create.name.label") }}</VLabel>
    <VInput
      ref="input"
      v-model="model"
      autofocus
      :placeholder="t('units.create.name.placeholder')"
      required="true"
      type="text"
    />
  </VFieldSet>
</template>
