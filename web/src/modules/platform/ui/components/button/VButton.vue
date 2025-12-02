<template>
  <router-link
    v-if="to"
    class="button"
    :class="styleClasses"
    :tabindex="disabled ? -1 : 0"
    :to="to"
    type="button"
    v-bind="{ disabled }"
    @click="$emit('click')"
    @keydown.enter="$emit('keydown', $event)"
  >
    <slot />
  </router-link>
  <button
    v-else
    class="button"
    :class="styleClasses"
    :tabindex="disabled ? -1 : 0"
    type="button"
    v-bind="{ disabled }"
    @click="$emit('click', $event)"
    @keydown.enter="$emit('keydown', $event)"
  >
    <slot />
  </button>
</template>

<script setup lang="ts">
import type {RouteLocationRaw} from "vue-router";
import {computed} from "vue";

type Props = {
  variant: "default" | "primary"
  disabled?: boolean
  outlined?: true
  flat?: true
  block?: true
  to?: RouteLocationRaw
}

const props = withDefaults(defineProps<Props>(), {
    variant: "default"
})

defineEmits(["click", "keydown"]);

const styleClasses = computed<unknown>(() => [
    `button--${props.variant}`,
    {
        'button--flat': props.flat,
        'button--outlined': props.outlined,
        'button--block': props.block
    }
])
</script>
