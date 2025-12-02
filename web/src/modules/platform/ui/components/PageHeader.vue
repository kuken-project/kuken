<template>
  <div
    :class="[
      $style.root,
      {
        [$style.noDivider]: noDivider,
        [$style.rowAlignment]: rowAlignment
      }
    ]"
  >
    <div :class="$style.text">
      <h4 :class="$style.title">
        <slot name="title">
          <span
            v-if="titleTranslationKey"
          >titleTranslationKey</span>
        </slot>
      </h4>
      <VBody2
        v-if="$slots.subtitle || subtitleTranslationKey"
        :class="$style.subtitle"
      >
        <slot name="subtitle">
          <span
            v-if="subtitleTranslationKey"
          >subtitleTranslationKey</span>
        </slot>
      </VBody2>
    </div>
    <div
      v-if="$slots.default"
      :class="$style.body"
    >
      <slot />
    </div>
  </div>
</template>
<script setup lang="ts">
import VBody2 from "@/modules/platform/ui/components/typography/VBody2.vue"

defineProps<{
  titleTranslationKey?: string
  subtitleTranslationKey?: string
  noDivider?: true
  rowAlignment?: true
}>()
</script>
<style lang="scss" module>
.root {
	margin: 4.8rem 0 2.4rem;
	padding-bottom: 1.6rem;
	border-bottom: 1px solid var(--kt-border-medium);
}

.noDivider {
	border-bottom: none;
	padding-bottom: 0;
}

.rowAlignment {
	display: flex;
}

.title {
	font-weight: 700;
	font-family: var(--kt-headline-font);
	user-select: none;
	color: var(--kt-content-neutral-high);
}

.subtitle {
	margin-top: 1.6rem;
	color: var(--kt-content-neutral);
	font-size: 14px;
}

.body {
	margin-top: 1.2rem;
}

.text {
	flex-grow: 1;
}
</style>
