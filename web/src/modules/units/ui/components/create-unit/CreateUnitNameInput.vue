<script lang="ts" setup>
import VFieldSet from "@/modules/platform/ui/components/form/VFieldSet.vue"
import VInput from "@/modules/platform/ui/components/form/VInput.vue"
import VLabel from "@/modules/platform/ui/components/form/VLabel.vue"

const model = defineModel<string>({ required: true })
</script>

<template>
  <VFieldSet>
    <VLabel>Server name</VLabel>
    <VInput
      ref="input"
      v-model="model"
      autofocus
      placeholder="Type your server name"
      required="true"
      type="text"
    />
  </VFieldSet>
</template>

<style lang="scss" scoped></style>
