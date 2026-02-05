<script lang="ts" setup>
import instancesService from "@/modules/instances/api/services/instances.service.ts"
import { useInstanceFilesStore } from "@/modules/instances/instance-files.store.ts"
import { useInstanceStore } from "@/modules/instances/instances.store.ts"
import InstanceFileList from "@/modules/instances/ui/components/files/browser/list/InstanceFileList.vue"
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import VLayout from "@/modules/platform/ui/components/grid/VLayout.vue"
import { useDropZone, useFileDialog } from "@vueuse/core"
import { nextTick, ref, useTemplateRef } from "vue"

const filePath = useInstanceFilesStore().getCurrentFilePath
const instance = useInstanceStore().getInstance

const dropZoneRef = useTemplateRef("dropZone")
const { isOverDropZone } = useDropZone(dropZoneRef, { onDrop: uploadFiles })
const fileDialog = useFileDialog({})
const refreshing = ref(false)

fileDialog.onChange((files) => {
  if (files == null) return

  const fileArray = new Array<File>()
  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    if (file != null) {
      fileArray.push(file)
    }
  }

  uploadFiles(fileArray)
})

async function uploadFiles(files: File[] | null) {
  if (files == null || files.length == 0) return

  const formData = new FormData()
  let added = false
  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    if (file == null) continue

    formData.append("files", file, file.name)
    added = true
  }

  if (!added) return

  await instancesService.uploadFiles(instance.id, filePath, formData)

  refreshing.value = true
  await nextTick(() => (refreshing.value = false))
}
</script>

<template>
  <div class="file-browser">
    <!--    <div ref="dropZone" class="dropZone" :class="{ active: isOverDropZone }">-->
    <!--      Drop files here: {{ isOverDropZone }}-->
    <!--    </div>-->
    <div class="header">
      <VLayout direction="horizontal" gap="sm">
        <VButton variant="default" @click="$router.back()">Go back</VButton>
        <VButton variant="primary" @click="fileDialog.open">Upload file</VButton>
      </VLayout>
    </div>
    <InstanceFileList
      v-if="!refreshing"
      :style="{ zIndex: 2 }"
      :instance-id="instance.id"
      :file-path="filePath ?? ''"
    />
  </div>
</template>

<style lang="scss" scoped>
.dropZone {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.dropZone.active {
  background-color: rgba(0, 0, 0, 0.5);
}

.file-browser {
  .header {
    padding: 1.6rem;
  }
}
</style>
