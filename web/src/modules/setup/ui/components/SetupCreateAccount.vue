<script setup lang="ts">
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import VFieldSet from "@/modules/platform/ui/components/form/VFieldSet.vue"
import VForm from "@/modules/platform/ui/components/form/VForm.vue"
import VInput from "@/modules/platform/ui/components/form/VInput.vue"
import VLabel from "@/modules/platform/ui/components/form/VLabel.vue"

const email = defineModel("email", { required: true, default: "" })
const password = defineModel("password", { required: true, default: "" })
const emit = defineEmits(["done"])
</script>

<template>
  <VForm @submit.prevent="emit('done')">
    <VFieldSet>
      <VLabel>
        Email
        <VInput v-model="email" type="email" required="true" autocomplete="email" />
      </VLabel>
      <VLabel>
        Password
        <VInput
          v-model="password"
          type="password"
          required="true"
          autocomplete="current-password"
        />
      </VLabel>
    </VFieldSet>
    <VButton type="submit" variant="primary" style="align-self: end"> Proceed </VButton>
  </VForm>
</template>

<style scoped lang="scss"></style>
