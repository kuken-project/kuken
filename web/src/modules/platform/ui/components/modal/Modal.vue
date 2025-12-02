<template>
  TODO bota a porra do modal aq
<!--  <vue-final-modal-->
<!--    v-slot="{ params, close }"-->
<!--    v-bind="$attrs"-->
<!--    classes="modal-container"-->
<!--    content-class="modal-content"-->
<!--  >-->
<!--    <span class="modal__title">-->
<!--      <slot name="title" />-->
<!--    </span>-->
<!--    <div class="modal__content">-->
<!--      <slot :params="params" />-->
<!--    </div>-->
<!--    <div-->
<!--      v-show="$slots.actions"-->
<!--      class="modal__action"-->
<!--    >-->
<!--      <slot name="actions" />-->
<!--    </div>-->
<!--    <button-->
<!--      class="modal__close"-->
<!--      @click="close"-->
<!--    >-->
<!--      <VIcon name="Close" />-->
<!--    </button>-->
<!--  </vue-final-modal>-->
</template>
<script setup lang="ts">
defineOptions({ inheritAttrs: false })
defineEmits(["confirm", "cancel"])
</script>