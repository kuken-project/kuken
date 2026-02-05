<template>
  <VLabel>
    <slot />
    <input v-model="model" type="checkbox" />
    <span></span>
    <VIcon name="Check" />
  </VLabel>
</template>

<script lang="ts" setup>
import VLabel from "@/modules/platform/ui/components/form/VLabel.vue"
import VIcon from "@/modules/platform/ui/components/icons/VIcon.vue"

const model = defineModel({ type: Boolean })
</script>

<style lang="scss" scoped>
label {
  display: flex;
  align-content: space-between;
  position: relative;

  &:hover {
    cursor: pointer;

    .title {
      opacity: 0.78;
    }

    input[type="checkbox"]:not(:checked) + span {
      border-color: var(--kt-border-low);
    }
  }
}

label input {
  opacity: 0 !important;
  display: block;
  height: 0;
  width: 0;
  position: absolute;
  overflow: hidden;
}

$size: 20px;

label span {
  height: $size;
  width: $size;
  border: 1.5px solid var(--kt-border-medium);
  border-radius: 4px;
  transition:
    background-color linear 0.15s,
    border linear 0.15s;
}

input[type="checkbox"]:not(:checked) + * + .icon {
  display: none;
}

input[type="checkbox"]:checked {
  & + * + .icon {
    fill: var(--kt-content-primary-oncolor);
  }

  & + span {
    border-color: transparent;
    background-color: var(--kt-content-primary);
  }
}

label .icon {
  $iconSize: calc($size - 8px);
  height: $iconSize;
  width: $iconSize;
  min-height: $iconSize;
  min-width: $iconSize;
  position: absolute;
  right: 4px;
  top: 4px;
}
</style>
