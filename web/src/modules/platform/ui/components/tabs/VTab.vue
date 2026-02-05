<template>
  <section
    v-show="isActive"
    :id="`tabpanel-${id}`"
    :aria-hidden="!isActive"
    :tabindex="isActive ? 0 : -1"
    role="tabpanel"
  >
    <slot />
  </section>
</template>

<script setup lang="ts">
import { TabsInjectionKey } from "@/modules/platform/ui/components/InjectionKeys"
import type { Tab } from "@/modules/platform/ui/components/tabs/VTabList.vue"
import { computed, inject, onBeforeMount, onBeforeUnmount } from "vue"

const props = defineProps<{
  id: string
  label: string
  disabled?: true
  icon?: string
}>()

// State
const internalId = computed(() => props.id.toLowerCase().replace(/ /g, "-"))
const hash = computed(() => `#${props.disabled ? "" : internalId.value}`)
const isActive = computed(() => tabs!.isTabActive(hash.value))

// Effects
const tabs = inject<{
  addTab(tab: Partial<Tab>): void
  removeTab(id: string): void
  isTabActive(hash: string): boolean
}>(TabsInjectionKey)

onBeforeMount(() => {
  tabs!.addTab({
    id: internalId.value,
    name: props.id,
    label: props.label,
    hash: hash.value,
    isActive: false,
    isDisabled: props.disabled,
    icon: props.icon
  })
})

onBeforeUnmount(() => tabs!.removeTab(internalId.value))
</script>
