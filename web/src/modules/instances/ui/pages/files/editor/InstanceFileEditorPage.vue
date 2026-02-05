<script setup lang="ts">
import instancesService from "@/modules/instances/api/services/instances.service.ts"
import { useInstanceFilesStore } from "@/modules/instances/instance-files.store.ts"
import { useInstanceStore } from "@/modules/instances/instances.store.ts"
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import VLayout from "@/modules/platform/ui/components/grid/VLayout.vue"
import Resource from "@/modules/platform/ui/components/Resource.vue"
import { cybrh3, isUndefined } from "@/utils"
import { usePreferredDark } from "@vueuse/core"
import { computed, onMounted, ref } from "vue"
import CodeMirror from "vue-codemirror6"

const instance = useInstanceStore().getInstance
const filePath = useInstanceFilesStore().getCurrentFilePath
const fileContents = ref<string>("")
const fileHash = ref<number>()
const darkMode = usePreferredDark()

function onContentsLoaded(contents: string) {
  if (isUndefined(fileHash.value)) {
    fileHash.value = cybrh3(contents)
  }

  fileContents.value = contents
}

const canSave = computed(
  () => !isUndefined(fileHash.value) && fileHash.value !== cybrh3(fileContents.value)
)

function onSave() {
  // TODO Handle save errors
  instancesService.replaceFileContents(instance.id, filePath, fileContents.value).then(() => {
    window.location.reload()
  })
}

const store = useInstanceFilesStore()
onMounted(() => {
  store.setCurrentDirectory(filePath)
})
</script>

<template>
  <div class="header">
    <VLayout gap="sm" direction="horizontal">
      <VButton variant="default" :to="store.getPreviousPathAsRouteLink()">Go back</VButton>
      <VButton variant="primary" :disabled="!canSave" @click.prevent="onSave">Save</VButton>
      {{ filePath }}
    </VLayout>
  </div>
  <div class="editor">
    <Resource
      :resource="() => instancesService.getFileContents(instance.id, filePath)"
      @loaded="onContentsLoaded"
    >
      <div class="contents">
        <CodeMirror v-model="fileContents" :dark="darkMode" basic tab allow-multiple-selections />
      </div>
    </Resource>
  </div>
</template>

<style scoped lang="scss">
.editor {
  position: relative;
  height: 100%;
}

.contents {
  height: 100%;

  :deep(.cm-content) {
    padding: 0 12px;
  }

  &,
  :deep(.cm-gutters),
  :deep(.cm-content) {
    background-color: var(--kt-background-surface) !important;
  }

  .vue-codemirror,
  .vue-codemirror:deep(.cm-editor) {
    height: 100%;
  }

  :deep(.cm-focused) {
    outline: none;
  }
}

.header {
  padding: 1.6rem;
}
</style>
