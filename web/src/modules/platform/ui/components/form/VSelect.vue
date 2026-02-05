<script lang="ts" setup>
import VIcon from "@/modules/platform/ui/components/icons/VIcon.vue"
import { onClickOutside } from "@vueuse/core"
import { computed, ref, useSlots, useTemplateRef, type VNode, type VNodeArrayChildren } from "vue"

const visible = ref(false)
const props = defineProps({
  text: { type: String, default: "Toggle dropdown" },
  options: Array
})
const options = ref({})

const $slots = useSlots()
const model = defineModel()

const renderOptions = () => {
  options.value = {}
  const root = $slots.default!()[0]! as VNode
  return (root.children as VNodeArrayChildren)?.map((vnode) => {
    options.value[vnode.props!.key] = vnode?.children?.default
    // noinspection JSUnusedGlobalSymbols
    Object.assign(
      (vnode.props ??= {}),
      { onClick: () => select(vnode.props!.key) },
      vnode.props ?? {}
    )
    return vnode
  })
}

function select(option: string) {
  console.log("selected", option)
  visible.value = false
  model.value = option
}

const selectionText = computed(() => {
  const selectedCmp = $slots.default!()[0]!.children.find(
    (vnode) => vnode.props!.key === model.value
  )

  return selectedCmp?.props?.value
})

const container = useTemplateRef("container")

onClickOutside(container, () => {
  if (!visible.value) return
  visible.value = false
})
</script>

<template>
  <div ref="container" class="select">
    <div ref="container" class="text" @click.stop="visible = !visible">
      <span>{{ selectionText ?? "Select a option" }}</span>
      <div class="icon-wrapper">
        <transition mode="out-in" name="rotate">
          <VIcon v-if="visible" name="AngleUp" />
          <VIcon v-else name="AngleDown" />
        </transition>
      </div>
    </div>
    <transition name="slide-up">
      <div v-if="visible" class="options">
        <render-options />
      </div>
    </transition>
  </div>
</template>

<style lang="scss" scoped>
.select {
  position: relative;
  border: 1px solid var(--kt-border-low);
  border-radius: 8px;
  background-color: var(--kt-background-body);

  .text {
    padding: 0.8rem 1.2rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 1.2rem;
    cursor: pointer;
  }

  .icon {
    width: 20px;
    height: 20px;
    position: absolute;
    fill: var(--kt-content-neutral);
  }

  .icon-wrapper {
    width: 20px;
    height: 20px;
    display: block;
  }
}

.options {
  position: absolute;
  border: 1px solid var(--kt-border-low);
  border-radius: 20px;
  background-color: var(--kt-background-body);
  padding: 0.8rem;
  transform: translateY(0.8rem);
  z-index: 1;
  min-width: 200px;
  right: 0;
}

.rotate-enter-active {
  animation: rotate-animation 0.1s forwards;
}

.rotate-leave-active {
  animation: rotate-animation 0.1s reverse forwards;
}

@keyframes rotate-animation {
  0% {
    opacity: 0;
    transform: scaleY(-1);
  }
  100% {
    opacity: 1;
    transform: scaleY(1);
  }
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.15s ease-out;
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(5px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-15px);
}
</style>
