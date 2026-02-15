<script lang="ts" setup>
import { useHead } from "@unhead/vue"
import { useI18n } from "petite-vue-i18n"

const { t } = useI18n()
useHead({
  title: t("blueprints.home.docTitle")
})
</script>

<template>
  <router-view />
</template>

<style lang="scss" scoped></style>
