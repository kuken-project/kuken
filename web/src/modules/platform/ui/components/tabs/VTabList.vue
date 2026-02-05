<template>
  <ul :aria-label="label" class="tab-list" role="tablist">
    <li v-for="(tab, idx) in tabs" :key="idx" role="presentation">
      <a
        :aria-controls="tab.hash"
        :aria-selected="tab.isActive"
        :class="{ 'tab--active': tab.isActive }"
        :href="tab.hash"
        class="tab"
        role="tab"
        @click="selectTab(tab.hash, $event)"
      >
        <VIcon v-if="tab.icon" :name="tab.icon" />
        <span v-text="tab.label" />
      </a>
    </li>
  </ul>
  <slot />
</template>

<script lang="ts"></script>
<script lang="ts" setup>
import VIcon from "@/modules/platform/ui/components/icons/VIcon.vue"
import { TabsInjectionKey } from "@/modules/platform/ui/components/InjectionKeys"
import { isUndefined } from "@/utils"
import { onMounted, provide, reactive, ref } from "vue"

export type Tab = {
  id: string
  index: number
  name: string
  hash: string
  label: string
  icon: string | undefined
  isActive: boolean
  isDisabled: boolean
}

defineProps<{ label: string }>()
const emits = defineEmits<{
  changed: [tab: Tab]
  clicked: [tab: Tab]
}>()

// State
const tabs = reactive<Tab[]>([])
const currActiveHash = ref("")
const lastActiveHash = ref("")

// Functions
function selectTab(hash: string, event?: Event) {
  const targetTab = findTab(hash)

  if (isUndefined(targetTab)) return
  if (!isUndefined(event) && targetTab.isDisabled) {
    event.preventDefault()
    return
  }

  if (lastActiveHash.value === currActiveHash.value) {
    emits("clicked", targetTab)
    return
  }

  tabs.forEach((tab: Tab) => (tab.isActive = tab.hash === targetTab.hash))
  lastActiveHash.value = currActiveHash.value = targetTab.hash
  emits("changed", targetTab)
}

function findTab(hash: string): Tab | undefined {
  return tabs.find((tab: Tab) => tab.hash === hash)
}

// Effects
onMounted(() => {
  window.addEventListener("hashchange", () => {
    selectTab(window.location.hash)
  })

  const urlHash = window.location.hash
  if (!isUndefined(findTab(urlHash))) {
    selectTab(urlHash)
    return
  }

  if (tabs.length > 0) selectTab(tabs[0]!.hash)
})

provide(TabsInjectionKey, {
  addTab(tab: Tab): void {
    tabs.push({
      ...tab,
      index: tabs.length
    })
  },
  updateTab(id: string, payload: Partial<Tab>): void {
    const idx = tabs.findIndex((tab: Tab) => tab.id === id)
    if (idx === -1) throw new Error(`Unknown tab: ${id}`)

    Object.assign(tabs[idx]!, payload)
  },
  removeTab(id: string) {
    const idx = tabs.findIndex((tab: Tab) => tab.id === id)
    if (idx === -1) throw new Error(`Unknown tab: ${id}`)

    tabs.slice(idx, 1)
  },
  isTabActive(hash: string): boolean {
    return currActiveHash.value === hash
  }
})
</script>
