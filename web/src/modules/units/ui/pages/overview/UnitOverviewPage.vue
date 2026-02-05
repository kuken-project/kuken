<script lang="ts" setup>
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import VContainer from "@/modules/platform/ui/components/grid/VContainer.vue"
import VLayout from "@/modules/platform/ui/components/grid/VLayout.vue"
import PageWrapper from "@/modules/platform/ui/components/PageWrapper.vue"
import VTitle from "@/modules/platform/ui/components/typography/VTitle.vue"
import { useUnitStore } from "@/modules/units/units.store.ts"

const unit = useUnitStore().getUnit
</script>

<template>
  <PageWrapper>
    <VContainer>
      <VTitle>{{ unit.name }}</VTitle>
      <VLayout gap="sm" direction="horizontal">
        <VButton
          :to="{
            name: 'instance.console',
            params: { unitId: unit.id, instanceId: unit.instance.id }
          }"
          variant="primary"
          >Console</VButton
        >
        <VButton
          :to="{
            name: 'instance.files',
            params: { instanceId: unit.instance.id }
          }"
          variant="default"
          >Files</VButton
        >
      </VLayout>
    </VContainer>
  </PageWrapper>
</template>

<style lang="scss" scoped></style>
