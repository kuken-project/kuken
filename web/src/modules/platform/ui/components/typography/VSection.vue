<script setup lang="ts"></script>
<template>
  <section>
    <h5><slot name="title" /></h5>
    <p v-if="$slots.description" class="description"><slot name="description" /></p>
    <div class="content">
      <slot />
    </div>
  </section>
</template>
<style lang="scss">
section {
  margin-bottom: 3.2rem;

  .description {
    color: var(--kt-content-neutral);
    margin-bottom: 2.4rem;
    margin-top: 0.4rem;
  }

  .content {
    margin-top: 1.6rem;
  }
}
</style>
