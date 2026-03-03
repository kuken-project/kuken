<script lang="ts" setup>
import { type Blueprint, iconAsBase64PNG } from "@/modules/blueprints/api/models/blueprint.model.ts"
import blueprintsService from "@/modules/blueprints/api/services/blueprints.service.ts"
import VIcon from "@/modules/platform/ui/components/icons/VIcon.vue"
import Resource from "@/modules/platform/ui/components/Resource.vue"
import { isNull } from "@/utils"
import { useI18n } from "petite-vue-i18n"
import { computed, ref, unref } from "vue"

const { t } = useI18n()
const selected = defineModel<string | null>()
const search = ref("")

const resource = () => blueprintsService.listReadyToUseBlueprints()
const blueprintList = ref<Blueprint[]>([])

const filtered = computed(() => {
  const query = search.value.trim().toLowerCase()
  if (!query) return blueprintList.value
  return blueprintList.value.filter(
    (bp) =>
      bp.header.name.toLowerCase().includes(query) || bp.header.author.toLowerCase().includes(query)
  )
})

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
  <Resource :resource="resource" @loaded="(value: Blueprint[]) => (blueprintList = value)">
    <template v-if="blueprintList">
      <div :class="$style.selector">
        <div :class="$style.toolbar">
          <input
            v-model="search"
            :class="$style.search"
            class="input"
            type="text"
            :placeholder="t('units.create.blueprint.searchPlaceholder')"
          />
          <span :class="$style.count">{{
            t("units.create.blueprint.available", { count: filtered.length })
          }}</span>
        </div>
        <div :class="$style.list">
          <div
            v-for="blueprint in filtered"
            :key="blueprint.id"
            :class="[$style.bp, isSelected(blueprint) && $style.bpSelected]"
            @click="select(blueprint)"
          >
            <div :class="$style.bpInner">
              <div
                :style="`background-image: url(${iconAsBase64PNG(blueprint.header.icon)})`"
                :class="$style.icon"
              />
              <div :class="$style.info">
                <div :class="$style.nameRow">
                  <span :class="$style.name">{{ blueprint.header.name }}</span>
                  <span v-if="blueprint.official" :class="$style.official">
                    <VIcon name="Verified" />
                  </span>
                </div>
                <span :class="$style.meta">{{ blueprint.header.version }}</span>
              </div>
            </div>
            <div v-if="isSelected(blueprint)" :class="$style.check">
              <VIcon name="Checkmark" />
            </div>
          </div>
        </div>
        <div v-if="filtered.length === 0" :class="$style.empty">
          {{ t("units.create.blueprint.noResults", { query: search }) }}
        </div>
      </div>
    </template>
  </Resource>
</template>

<style module lang="scss">
.selector {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search {
  flex: 1;
}

.count {
  font-weight: 500;
  color: var(--kt-content-neutral-low);
  white-space: nowrap;
  flex-shrink: 0;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 480px;
  overflow-y: auto;
  padding-right: 4px;
}

.empty {
  padding: 32px 0;
  text-align: center;
  color: var(--kt-content-neutral-low);
}

.bp {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--kt-border-low);
  border-radius: 12px;
  cursor: pointer;
  transition:
    border-color 0.15s ease,
    background-color 0.15s ease,
    box-shadow 0.15s ease;

  &:hover {
    border-color: var(--kt-border-medium);
    background-color: var(--kt-background-surface);
  }
}

.bpSelected {
  border-color: var(--kt-content-primary);
  background-color: rgba(255, 116, 56, 0.04);

  &:hover {
    border-color: var(--kt-content-primary);
    background-color: rgba(255, 116, 56, 0.04);
  }
}

.bpInner {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
  min-width: 0;
}

.icon {
  width: 44px;
  height: 44px;
  min-width: 44px;
  border-radius: 10px;
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  background-color: var(--kt-background-surface-high);
}

.info {
  display: flex;
  flex-direction: column;
  gap: 1px;
  flex: 1;
  min-width: 0;
}

.nameRow {
  display: flex;
  align-items: center;
  gap: 6px;
}

.name {
  font-weight: 600;
  color: var(--kt-content-neutral-high);
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta {
  color: var(--kt-content-neutral-low);
}

.official {
  display: inline-flex;
  align-items: center;
  color: #4b7bec;

  :deep(svg),
  :deep(.icon) {
    width: 14px;
    height: 14px;
    fill: #4b7bec;
  }
}

.check {
  width: 22px;
  height: 22px;
  min-width: 22px;
  border-radius: 6px;
  background-color: var(--kt-content-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 2px;

  :deep(svg),
  :deep(.icon) {
    width: 12px;
    height: 12px;
  }
}
</style>
