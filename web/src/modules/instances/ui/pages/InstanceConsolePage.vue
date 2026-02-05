<script lang="ts" setup>
import { useInstanceStore } from "@/modules/instances/instances.store.ts"
import InstanceConsole from "@/modules/instances/ui/components/InstanceConsole.vue"
import InstanceConsoleLayout from "@/modules/instances/ui/layouts/InstanceConsoleLayout.vue"
import { useUnitStore } from "@/modules/units/units.store.ts"
import { useHead } from "@unhead/vue"

const unit = useUnitStore().getUnit
useHead({
  title: `${unit.name} - Instance Console`
})

const instance = useInstanceStore().getInstance
</script>

<template>
  <InstanceConsoleLayout>
    <div class="console-container">
      <InstanceConsole :instance-id="instance.id" />
    </div>
  </InstanceConsoleLayout>
</template>

<style scoped>
.console-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  color: #d4d4d4;
  font-family: "JetBrains Mono", monospace;
  font-size: 14px;
}

.console-wrapper {
  flex: 1;
  position: relative;
  overflow: hidden;
}
</style>
