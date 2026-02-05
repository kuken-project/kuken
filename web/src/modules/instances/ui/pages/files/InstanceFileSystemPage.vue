<script setup lang="ts">
import InstanceFileBrowserTree from "@/modules/instances/ui/components/files/browser/tree/InstanceFileBrowserTree.vue"
</script>

<template>
  <div class="file-system">
    <InstanceFileBrowserTree />
    <div class="file-system-inner">
      <router-view v-slot="{ Component, route }">
        <component :is="Component" :key="route.fullPath" />
      </router-view>
    </div>
  </div>
</template>

<style scoped lang="scss">
.file-system {
  display: flex;
  flex-direction: row;
  height: 100%;
  overflow: hidden;
}

.file-system-inner {
  display: flex;
  flex-direction: column;
  flex: 1;
}
</style>
