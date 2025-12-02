<template>
  <VContainer>
    <h1>home.title</h1>
  </VContainer>
</template>

<script setup lang="ts">
import { useAccountsStore } from "@/modules/accounts/accounts.store"
import VContainer from "@/modules/platform/ui/components/grid/VContainer.vue"

const account = useAccountsStore().getAccount
</script>