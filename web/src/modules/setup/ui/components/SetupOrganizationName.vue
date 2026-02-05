<script setup lang="ts">
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import VFieldSet from "@/modules/platform/ui/components/form/VFieldSet.vue"
import VForm from "@/modules/platform/ui/components/form/VForm.vue"
import VInput from "@/modules/platform/ui/components/form/VInput.vue"
import VLabel from "@/modules/platform/ui/components/form/VLabel.vue"

const organizationName = defineModel("organizationName", { required: true, default: "" })
const emit = defineEmits(["done"])
</script>

<template>
  <VForm @submit.prevent="emit('done')">
    <VFieldSet>
      <VLabel>
        Organization Name
        <VInput v-model="organizationName" type="text" required="true" />
      </VLabel>
    </VFieldSet>
    <VButton type="submit" variant="primary" style="align-self: end"> Proceed </VButton>
  </VForm>
</template>

<style scoped lang="scss"></style>
