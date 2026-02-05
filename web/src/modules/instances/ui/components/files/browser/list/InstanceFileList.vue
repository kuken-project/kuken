<script setup lang="ts">
import type { VirtualFile } from "@/modules/instances/api/models/file.model.ts"
import instancesService from "@/modules/instances/api/services/instances.service.ts"
import { useInstanceFilesStore } from "@/modules/instances/instance-files.store.ts"
import InstanceFileListItemDirectory from "@/modules/instances/ui/components/files/browser/list/InstanceFileListItemDirectory.vue"
import InstanceFileListItemRegularFile from "@/modules/instances/ui/components/files/browser/list/InstanceFileListItemRegularFile.vue"
import VContainer from "@/modules/platform/ui/components/grid/VContainer.vue"
import Resource from "@/modules/platform/ui/components/Resource.vue"
import { useObjectUrl } from "@vueuse/core"
import { nextTick, onMounted, ref } from "vue"
import { useRouter } from "vue-router"

const { instanceId, filePath } = defineProps<{
  instanceId: string
  filePath: string
}>()

const fileList = ref<VirtualFile[]>()
const refreshing = ref(false)

function refresh() {
  refreshing.value = true
  nextTick(() => (refreshing.value = false))
}

const router = useRouter()
// const links = computed(() =>
//   filePath.split("/").map((query) => {
//     return {
//       path: query,
//       route: router.resolve({
//         name: "instance.files",
//         params: { instanceId },
//         query: { filePath: query }
//       })
//     }
//   })
// )

function downloadFile(file: VirtualFile) {
  fileUrl.value = useObjectUrl(file).value

  // 3. Programmatically trigger the download using an anchor element
  const link = document.createElement("a")
  link.href = fileUrl.value
  link.download = fileName.value // Set the desired file name
  link.style.display = "none"
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

function onFilesLoaded(files: VirtualFile[]) {
  useInstanceFilesStore().updateFiles(files)
  fileList.value = files.sort((a, b) => a.type.localeCompare(b.type))
}

onMounted(() => {
  useInstanceFilesStore().setCurrentDirectory(filePath)
})
</script>

<template>
  <VContainer>
    <Resource
      v-if="!refreshing"
      :resource="() => instancesService.listFiles(instanceId, filePath)"
      @loaded="onFilesLoaded"
    >
      <div class="file-list">
        <template v-for="file in fileList" :key="file.name">
          <InstanceFileListItemRegularFile v-if="file.type === 'FILE'" :file="file" />
          <InstanceFileListItemDirectory v-if="file.type === 'DIRECTORY'" :file="file" />
        </template>
      </div>
    </Resource>
  </VContainer>
</template>
<style scoped lang="scss">
.file-list {
  display: flex;
  flex-direction: column;
  padding: 0.8rem;
}

.file-list-container {
}
</style>
