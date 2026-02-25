<script setup lang="ts">
import { useAccount } from "@/modules/accounts/accounts.store.ts"
import ProfileOverviewLocaleSelector from "@/modules/accounts/ui/components/overview/locale/ProfileOverviewLocaleSelector.vue"
import ProfileOverviewThemeSelector from "@/modules/accounts/ui/components/overview/theme/ProfileOverviewThemeSelector.vue"
import PageWrapper from "@/modules/platform/ui/components/PageWrapper.vue"
import VContainer from "@/modules/platform/ui/components/grid/VContainer.vue"
import VSection from "@/modules/platform/ui/components/typography/VSection.vue"
import { useHead } from "@unhead/vue"
import { useI18n } from "petite-vue-i18n"

const account = useAccount()

const { t } = useI18n()

useHead({
  title: t("profile.docTitle", { email: account.value.email })
})
</script>

<template>
  <PageWrapper>
    <VContainer>
      <VSection>
        <template #title>{{ t("profile.pageTitle") }}</template>
        <template #description>{{ account.email }}</template>
      </VSection>
      <hr />
      <ProfileOverviewThemeSelector />
      <hr />
      <ProfileOverviewLocaleSelector />
    </VContainer>
  </PageWrapper>
</template>

<style scoped lang="scss"></style>
