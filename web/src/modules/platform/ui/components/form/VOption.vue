<script lang="ts" setup></script>
<template>
  <div class="option">
    <slot />
  </div>
</template>

<style lang="scss" scoped>
.option {
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background-color linear 0.15s;
  color: var(--kt-content-neutral);

  &:hover {
    color: var(--kt-content-neutral-high);
    background-color: var(--kt-background-surface-hover);
  }
}
</style>
