<template>
  <img
    alt="Katan"
    aria-label="Logo"
    :src="src"
  >
</template>
<script setup lang="ts">
import { computed } from "vue"

const props = defineProps<{ black?: boolean }>()
const src = computed(() => `/img/${props.black ? "logo-black" : "logo"}.svg`)
</script>