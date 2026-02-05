<script lang="ts" setup>
import TheHeader from "@/modules/platform/ui/components/TheHeader.vue"
import Breadcrumb from "@/modules/platform/ui/components/Breadcrumb.vue"
</script>
<template>
  <div class="layout">
    <!-- <TheSidebar /> -->
    <div class="layout-main">
      <TheHeader />
      <div class="layout-content">
        <slot />
      </div>
    </div>
  </div>
</template>
<style lang="scss" scoped>
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.layout-main {
  display: flex;
  flex-grow: 1;
  flex-direction: column;
  overflow-y: hidden;
  background-color: #d85f34;
  height: 100%;
}

.layout-content {
  background-color: var(--kt-background-body);
  border-radius: 20px;
  margin: 0 16px 16px 16px;
  height: 100%;
  overflow-y: auto;
  overflow-x: auto;
}
</style>
