<script lang="ts" setup>
import type { VirtualFile } from "@/modules/instances/api/models/file.model.ts"
import instancesService from "@/modules/instances/api/services/instances.service.ts"
import { useInstanceFilesStore } from "@/modules/instances/instance-files.store.ts"
import { useInstanceStore } from "@/modules/instances/instances.store.ts"
import VIcon from "@/modules/platform/ui/components/icons/VIcon.vue"
import { computedAsync } from "@vueuse/core"
import { onUnmounted } from "vue"
import { useRouter } from "vue-router"

const props = defineProps<{
  node: VirtualFile
  depth?: number
  destroyed?: () => void
}>()
const depth = props.depth ?? 0

const store = useInstanceFilesStore()
const router = useRouter()

function onClick() {
  if (props.node.type === "DIRECTORY") {
    store.toggleExpand(props.node.relativePath)
  } else {
    store.select(props.node.relativePath)
    router.push({
      name: "instance.file.editor",
      query: {
        filePath: props.node.relativePath
      }
    })
  }
}

function onChildLeave(child: VirtualFile) {
  if (store.isExpanded(child.relativePath)) {
    store.toggleExpand(child.relativePath)
  }
}

const fileList = computedAsync(
  async () => {
    const instanceId = useInstanceStore().getInstance.id
    const files = await instancesService.listFiles(instanceId, props.node.relativePath)
    return files.sort((a, b) => a.type.localeCompare(b.type))
  },
  null,
  { lazy: true, flush: "sync" }
)

onUnmounted(() => {
  props.destroyed?.()
})
</script>

<template>
  <div class="tree-node">
    <div
      class="node-content"
      :style="{ marginLeft: depth === 0 ? '8px' : `${depth * 16}px` }"
      :class="{ selected: store.selectedId === node.relativePath }"
      @click="onClick"
    >
      <span class="icon-wrapper">
        <template v-if="node.type === 'DIRECTORY'">
          <VIcon v-if="store.isExpanded(node.relativePath)" name="FolderOpen" />
          <VIcon v-else name="Folder" />
        </template>
        <VIcon v-else name="File" />
      </span>
      <span class="name">
        {{ node.name }}
      </span>
    </div>
    <template v-if="node.type === 'DIRECTORY' && store.isExpanded(node.relativePath)">
      <InstanceFileBrowserTreeItem
        v-for="child in fileList"
        :key="child.relativePath"
        class="selected"
        :destroyed="() => onChildLeave(child)"
        :node="child"
        :depth="depth + 1"
      />
      <span
        v-if="fileList && fileList.length === 0"
        :style="{ marginLeft: depth === 0 ? '8px' : `${depth * 16}px` }"
        :class="$style.emtpy"
      >
        This directory is empty.
      </span>
    </template>
  </div>
</template>
<style lang="scss" scoped>
.node-content {
  display: flex;
  flex-direction: row;
  gap: 8px;
  align-items: center;
  cursor: pointer;
  padding: 0.4rem 0.8rem;
  margin: 0 0.8rem;
  border-radius: 4px;
  color: var(--kt-content-neutral);

  &:hover {
    background-color: var(--kt-background-surface-hover);
  }

  .icon {
    fill: var(--kt-content-neutral-low);
  }

  &.selected {
    cursor: default;
    background-color: var(--kt-background-surface-high);
    color: var(--kt-content-neutral-high);
  }

  .name {
    display: block;
    text-overflow: ellipsis;
    position: relative;
    overflow-x: hidden;
    white-space: nowrap;
    user-select: none;
  }

  .icon {
    min-width: 16px;
    min-height: 16px;
    width: 16px;
    height: 16px;
  }

  .icon-wrapper {
    width: 16px;
    height: 16px;
  }
}
</style>
<style lang="scss" module>
.emtpy {
  color: var(--kt-content-neutral-low);
  font-style: italic;
  font-size: 12px;
  left: 32px;
  top: -2px;
  position: relative;
}
</style>
