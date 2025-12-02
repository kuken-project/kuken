<template>
  <div :class="$style.root">
    <TheSidebar />
    <div :class="$style.body">
      <TheHeader />
      <slot />
    </div>
  </div>
</template>
<script setup lang="ts">
import TheHeader from "@/modules/platform/ui/components/TheHeader.vue"
import TheSidebar from "@/modules/platform/ui/components/TheSidebar.vue"
</script>
<style lang="scss" module>
.root {
	display: flex;
	height: 100%;
	overflow: hidden;
}

.body {
	display: flex;
	flex-grow: 1;
	flex-direction: column;
	overflow: auto;
}
</style>
