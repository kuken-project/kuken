<template>
  <section>
    <h4><slot name="title" /></h4>
    <p v-if="$slots.description" class="description"><slot name="description" /></p>
    <slot />
  </section>
</template>
<style lang="scss">
section {
  margin-bottom: 3.6rem;
}

.description {
  color: var(--kt-content-neutral);
  margin-bottom: 2.4rem;
}
</style>
