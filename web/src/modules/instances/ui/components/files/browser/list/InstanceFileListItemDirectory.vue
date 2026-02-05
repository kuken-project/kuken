<script setup lang="ts">
import type { VirtualFile } from "@/modules/instances/api/models/file.model.ts"
import VIcon from "@/modules/platform/ui/components/icons/VIcon.vue"
import dayjs from "dayjs"

defineProps<{ file: VirtualFile }>()
</script>

<template>
  <router-link
    :to="{
      name: 'instance.files',
      query: { filePath: file.relativePath }
    }"
    class="file"
  >
    <div class="icon-wrapper">
      <VIcon name="Folder" />
    </div>
    <div class="name">{{ file.name }}</div>
    <div class="createdAt">
      {{ dayjs(file.createdAt).format("DD/MMM/YYYY h:mm A") }}
    </div>
  </router-link>
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
}
</style>
