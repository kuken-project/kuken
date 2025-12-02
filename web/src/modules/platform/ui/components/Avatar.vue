<template>
  <div
    v-if="!src"
    class="avatar"
    :style="{
      backgroundImage: `url(${src})`
    }"
  />
  <ProgressiveImage
    v-else
    class="avatar"
    :src="src"
  />
</template>

<script setup lang="ts">
import { computed } from "vue"
import { isNull, isUndefined } from "@/utils"
import { ProgressiveImage } from "vue-progressive-image"

const props = defineProps<{ src: string | undefined | null }>()

const src = computed(() => isUndefined(props.src) || isNull(props.src)
    ? "/img/logo-black.svg"
    : props.src)
</script>