<template>
  <Component
    :is="component"
    class="icon"
    :aria-labelledby="`icon-${name}`"
  />
</template>
<script setup lang="ts">
import {defineAsyncComponent} from "vue";

const props = defineProps<{ name: string }>()
const component = defineAsyncComponent(() =>
    import(`./_${props.name}.vue`)
)
</script>