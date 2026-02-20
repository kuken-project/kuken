<script setup lang="ts"></script>
<template>
  <section>
    <h4><slot name="title" /></h4>
    <p v-if="$slots.description" class="description"><slot name="description" /></p>
    <div class="content">
      <slot />
    </div>
  </section>
</template>
<style lang="scss">
section {
  margin-bottom: 2.4rem;

  .description {
    color: var(--kt-content-neutral);
    margin-bottom: 2.4rem;
  }

  .content {
    margin-top: 1.6rem;
  }
}
</style>
