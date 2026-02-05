<template>
  <router-view />
</template>

<script lang="ts" setup>
import { usePlatformStore } from "@/modules/platform/platform.store.ts"
import { useHead } from "@unhead/vue"

const organizationName = usePlatformStore().getBackendInfo.organization.name
useHead({
  title: `Welcome to ${organizationName}`
})
</script>
