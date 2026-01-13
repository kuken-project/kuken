<script lang="ts" setup>
import VLabel from "@/modules/platform/ui/components/form/VLabel.vue"
import VFieldSet from "@/modules/platform/ui/components/form/VFieldSet.vue"
import VInput from "@/modules/platform/ui/components/form/VInput.vue"

const model = defineModel<string>({ required: true })
</script>

<template>
    <VFieldSet>
        <VLabel>Unit name</VLabel>
        <VInput v-model="model" placeholder="Type your server name" required="true" type="text" />
    </VFieldSet>
</template>

<style lang="scss" scoped></style>
