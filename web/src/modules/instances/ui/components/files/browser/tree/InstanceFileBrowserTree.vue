<script lang="ts" setup>
import { useInstanceFilesStore } from "@/modules/instances/instance-files.store.ts"
import InstanceFileBrowserTreeItem from "@/modules/instances/ui/components/files/browser/tree/InstanceFileBrowserTreeItem.vue"
import { computed, ref } from "vue"
import ResizeBounding from "vue3-resize-bounding"

const store = useInstanceFilesStore()
const fileList = computed(() => store.getTree)

const container = ref({ width: 320 })
</script>

<template>
  <ResizeBounding
    :width="container.width"
    :min-width="240"
    :max-width="480"
    :directions="'r'"
    class="tree"
    @update:width="
      (width) => {
        container.width = width
      }
    "
  >
    <ul class="files">
      <InstanceFileBrowserTreeItem
        v-for="file in fileList.sort((a, b) => a.type.localeCompare(b.type))"
        :key="file.name"
        :node="file"
      />
    </ul>
  </ResizeBounding>
</template>
<style scoped lang="scss">
.tree {
  position: relative;
  top: 0;
  bottom: 0;
  width: 240px;
  background: var(--kt-background-body);
  height: 100%;
  border-right: 2px solid var(--kt-border-low);
  overflow-y: auto;
  overflow-x: hidden;
  padding: 1.6rem 0.8rem;
}

.files {
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
</style>
