<script lang="ts" setup>
import { type Blueprint, iconAsBase64PNG } from "@/modules/blueprints/api/models/blueprint.model.ts"
import blueprintsService from "@/modules/blueprints/api/services/blueprints.service.ts"
import VIcon from "@/modules/platform/ui/components/icons/VIcon.vue"
import Resource from "@/modules/platform/ui/components/Resource.vue"
import { isNull } from "@/utils"
import { reactive, unref } from "vue"

const selected = defineModel<string | null>()

const resource = () => blueprintsService.listReadyToUseBlueprints()
const { blueprintList } = reactive<{ blueprintList: Blueprint[] }>({ blueprintList: [] })

function isSelected(blueprint: Blueprint) {
  return !isNull(selected) && unref(selected) === blueprint.id
}

function select(blueprint: Blueprint) {
  if (isSelected(blueprint)) {
    selected.value = null
  } else {
    selected.value = blueprint.id
  }
}
</script>

<template>
  <h4>Select a blueprint</h4>
  <Resource :resource="resource" @loaded="(value: Blueprint[]) => (blueprintList = value)">
    <template v-if="blueprintList">
      <ul class="blueprintList">
        <li
          v-for="blueprint in blueprintList"
          :key="blueprint.id"
          :class="{ selected: isSelected(blueprint) }"
          class="blueprint"
          @click="select(blueprint)"
        >
          <div
            :style="`background-image: url(${iconAsBase64PNG(blueprint.header.icon)})`"
            class="image"
          />
          <div class="body">
            <h5 class="title" v-text="blueprint.header.name" />
            <p class="description">
              Version {{ blueprint.header.version
              }}{{ blueprint.official ? " · Küken Official Blueprint" : "" }}
              <span class="icon">
                <VIcon name="Verified" />
              </span>
            </p>
          </div>
        </li>
      </ul>
    </template>
  </Resource>
</template>

<style lang="scss" scoped>
.blueprintList {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 1.2rem;
}

.blueprint {
  display: flex;
  flex-direction: row;
  gap: 16px;
  padding: 12px;
  border: 2px solid var(--kt-border-low);
  transition:
    border ease-in-out 0.15s,
    background-color ease-in-out 0.15s;
  border-radius: 20px;

  &:hover {
    cursor: pointer;
    border-color: var(--kt-border-medium);
  }

  &.selected {
    border-color: var(--kt-content-primary);
  }
}

.image {
  width: 72px;
  height: 72px;
  min-width: 72px;
  min-height: 72px;
  border-radius: 20px;
  display: block;
  background-size: cover;
  background-repeat: no-repeat;
}

.body {
  display: flex;
  flex-direction: column;
  padding: 8px 0;
  justify-content: center;
  gap: 0;

  .title {
    font-family: var(--kt-body-font), serif;
  }
}

.description {
  color: var(--kt-content-neutral);
  display: inline-flex;
  flex-direction: row;
  align-items: center;

  .icon {
    min-width: 16px;
    min-height: 16px;
    max-height: 16px;
    margin-left: 2px;
    fill: #4b7bec;
  }
}
</style>
