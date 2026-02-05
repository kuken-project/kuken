<script lang="ts" setup>
import Resource from "@/modules/platform/ui/components/Resource.vue"
import unitsService from "@/modules/units/api/services/units.service.ts"
import { useUnitStore } from "@/modules/units/units.store.ts"
import { isNull } from "@/utils"
import { useHead } from "@unhead/vue"
import { computed, onUnmounted } from "vue"

defineProps<{ unitId: string }>()

const unitStore = useUnitStore()
const unit = computed(() => unitStore.unit)

useHead({
  title: () => (!isNull(unitStore.unit) ? unitStore.getUnit.name : null)
})

onUnmounted(unitStore.resetUnit)
</script>

<template>
  <Resource :resource="() => unitsService.getUnit(unitId)" @loaded="unitStore.updateUnit">
    <router-view />
  </Resource>
</template>

<style lang="scss" scoped></style>
