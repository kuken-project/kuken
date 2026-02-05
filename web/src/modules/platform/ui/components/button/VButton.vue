<template>
  <router-link
    v-if="to"
    :class="styleClasses"
    :tabindex="disabled ? -1 : 0"
    :to="to"
    class="button"
    type="button"
    v-bind="{ disabled: disabled ? 'true' : undefined }"
    @click="$emit('click')"
    @keydown.enter="$emit('keydown', $event)"
  >
    <slot />
  </router-link>
  <button
    v-else
    :class="styleClasses"
    :tabindex="disabled ? -1 : 0"
    class="button"
    type="button"
    v-bind="{ disabled: disabled ? 'true' : undefined }"
    @click="$emit('click', $event)"
    @keydown.enter="$emit('keydown', $event)"
  >
    <slot />
  </button>
</template>

<script lang="ts" setup>
import { computed } from "vue"
import type { RouteLocationRaw } from "vue-router"

type Props = {
  variant: "default" | "primary"
  disabled?: boolean
  outlined?: true
  flat?: true
  block?: true
  // eslint-disable-next-line vue/require-default-prop
  to?: RouteLocationRaw
}

const props = withDefaults(defineProps<Props>(), {
  variant: "default"
})

defineEmits(["click", "keydown"])

const styleClasses = computed<unknown>(() => [
  `button--${props.variant}`,
  {
    "button--flat": props.flat,
    "button--outlined": props.outlined,
    "button--block": props.block
  }
])
</script>
