<script lang="ts" setup>
import type { BlueprintBuildInputCheckbox } from "@/modules/blueprints/api/models/blueprint.spec.model.ts"
import VCheckbox from "@/modules/platform/ui/components/form/VCheckbox.vue"
import VFieldSet from "@/modules/platform/ui/components/form/VFieldSet.vue"
import VLabel from "@/modules/platform/ui/components/form/VLabel.vue"
import VIcon from "@/modules/platform/ui/components/icons/VIcon.vue"

const props = defineProps<BlueprintBuildInputCheckbox>()
const model = defineModel()
</script>

<template>
  <VFieldSet>
    <VLabel>
      <div class="info">
        <div class="title">{{ props.label }}</div>
        <div v-if="props.description" class="description">{{ props.description }}</div>
      </div>
      <VCheckbox v-model="model" />
      <span></span>
      <VIcon name="Check" />
    </VLabel>
  </VFieldSet>
</template>

<style lang="scss" scoped>
fieldset:not(:last-child) {
  margin-bottom: 2.4rem;
}

label {
  display: flex;
  align-content: space-between;

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

$size: 24px;

label span {
  height: $size;
  width: $size;
  border: 1.5px solid var(--kt-border-medium);
  border-radius: 4px;
  position: absolute;
  right: 0;
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

.info {
  display: flex;
  flex-direction: column;
  flex-basis: 85%;
  justify-items: center;

  .title {
    transition: color linear 0.15s;
  }

  .description {
    color: var(--kt-content-neutral);
    font-weight: 400;
  }
}
</style>
