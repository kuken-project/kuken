<script setup lang="ts">
import type { VirtualFile } from "@/modules/instances/api/models/file.model.ts"
import { filesize } from "filesize"
import dayjs from "dayjs"
import VIcon from "@/modules/platform/ui/components/icons/VIcon.vue"
import {
  ContextMenu,
  ContextMenuItem,
  ContextMenuSeparator,
  type MenuOptions
} from "@imengyu/vue3-context-menu"
import { ref, shallowRef, useTemplateRef } from "vue"
import instancesService from "@/modules/instances/api/services/instances.service.ts"
import { useInstanceStore } from "@/modules/instances/instances.store.ts"
import { onClickOutside } from "@vueuse/core"
import VCheckbox from "@/modules/platform/ui/components/form/VCheckbox.vue"

const { file } = defineProps<{ file: VirtualFile }>()

const showContextMenu = ref(false)
const options = ref<MenuOptions>({
  zIndex: 3,
  minWidth: 230,
  x: 500,
  y: 200
})

function onContextMenu(e: MouseEvent) {
  e.preventDefault()
  showContextMenu.value = true
  options.value.x = e.x
  options.value.y = e.y
}

const renaming = ref(false)
const fileName = shallowRef(file.name)
const nameInput = useTemplateRef("name")

function onBeginRenameFile() {
  renaming.value = true
  nameInput.value?.blur()
}

async function onFinishRenameFile() {
  if (!renaming.value) return
  if (fileName.value.trim().length === 0 || fileName.value === file.name) {
    renaming.value = false
    return
  }

  try {
    const instanceId = useInstanceStore().getInstance.id
    console.log("props", instanceId, file.relativePath, fileName.value)

    await instancesService.renameFile(instanceId, file.relativePath, fileName.value)
  } finally {
    renaming.value = false
  }
}

const deleted = ref(false)
async function onDeleteFile() {
  const instanceId = useInstanceStore().getInstance.id

  await instancesService.deleteFile(instanceId, file.relativePath)
  deleted.value = true
}

onClickOutside(nameInput, () => {
  onFinishRenameFile()
})
</script>

<template>
  <component
    :is="renaming ? 'span' : 'router-link'"
    v-if="!deleted"
    :disabled="renaming"
    :to="{
      name: 'instance.file.editor',
      query: { filePath: file.relativePath }
    }"
    class="file"
    @contextmenu="onContextMenu"
  >
    <VCheckbox @click.stop />
    <div class="icon-wrapper"><VIcon name="File" /></div>
    <form v-if="renaming" @submit.prevent="onFinishRenameFile" @keydown.enter.stop>
      <input ref="name" v-model="fileName" type="text" class="name" />
    </form>
    <div v-else ref="name" class="name">
      {{ fileName }}
    </div>
    <div class="size">
      {{ filesize(file.size, { standard: "jedec" }) }}
    </div>
    <div class="createdAt">
      {{ dayjs(file.createdAt).format("DD/MMM/YYYY h:mm A") }}
    </div>
  </component>

  <context-menu v-model:show="showContextMenu" :options="options">
    <context-menu-item label="Rename" @click="onBeginRenameFile">
      <template #icon>
        <VIcon name="Rename" />
      </template>
    </context-menu-item>
    <context-menu-item label="Download">
      <template #icon>
        <VIcon name="Download" />
      </template>
    </context-menu-item>
    <context-menu-separator />
    <context-menu-item label="Delete" @click="onDeleteFile">
      <template #icon>
        <VIcon name="DeleteForever" />
      </template>
    </context-menu-item>
  </context-menu>
</template>

<style scoped lang="scss">
.file {
  display: flex;
  align-items: center;
  flex-direction: row;
  padding: 8px;
  border-radius: 8px;
  gap: 16px;
  text-decoration: none;

  .size,
  .createdAt {
    color: var(--kt-content-neutral);
  }

  &:hover {
    background-color: var(--kt-background-surface);
  }

  .icon-wrapper {
    fill: var(--kt-content-neutral-low);
    width: 24px;
    height: 24px;
  }

  .createdAt {
    margin-left: auto;
  }

  .name[contenteditable="true"] {
    cursor: default;
  }
}

input {
  border-radius: 4px;
  background-color: var(--kt-background-surface-high);
  height: 26px;
  padding: 0 8px;
  font-family: var(--kt-body-font), serif;
  font-size: 14px;
}
</style>
