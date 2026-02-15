<template>
  <router-view />
</template>

<script lang="ts" setup>
import { usePlatformStore } from "@/modules/platform/platform.store.ts"
import { useHead } from "@unhead/vue"
import { useI18n } from "petite-vue-i18n"

const organizationName = usePlatformStore().getBackendInfo.organization.name
const { t } = useI18n()

useHead({
  title: t("home.welcome.docTitle", { organizationName })
})
</script>
