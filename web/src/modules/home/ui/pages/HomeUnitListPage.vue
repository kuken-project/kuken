<template>
  <PageWrapper>
    <VContainer>
      <VTitle>Welcome</VTitle>
      <VCol :size="4">
        <div class="header">
          <h4>Your Server List</h4>
          <span class="count" v-text="state.units.length" />
        </div>
        <Resource
          :resource="unitsService.listUnits"
          @loaded="(data: Unit[]) => (state.units = data)"
        >
          <div class="serverList">
            <router-link
              v-for="unit in state.units"
              :key="unit.id"
              :to="{
                name: 'unit',
                params: { unitId: unit.id }
              }"
              class="serverListItem"
            >
              <ProgressiveImage
                :src="resolveBlueprintSource(unit.instance.blueprint.header.assets.icon)"
                class="image"
              />
              <div class="body">
                <h5 class="title" v-text="unit.name" />
                <p class="description">
                  {{ unit.instance.blueprint.header.name }}
                  <span class="icon">
                    <VIcon name="Verified" />
                  </span>
                </p>
              </div>
            </router-link>
          </div>
        </Resource>
      </VCol>
      <VCol :size="4">
        <VButton to="blueprints" variant="primary"> Go to blueprints </VButton>
      </VCol>
    </VContainer>
  </PageWrapper>
</template>

<script lang="ts" setup>
import { resolveBlueprintSource } from "@/modules/blueprints/api/models/blueprint.model.ts"
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import VCol from "@/modules/platform/ui/components/grid/VCol.vue"
import VContainer from "@/modules/platform/ui/components/grid/VContainer.vue"
import VIcon from "@/modules/platform/ui/components/icons/VIcon.vue"
import PageWrapper from "@/modules/platform/ui/components/PageWrapper.vue"
import Resource from "@/modules/platform/ui/components/Resource.vue"
import VTitle from "@/modules/platform/ui/components/typography/VTitle.vue"
import type { Unit } from "@/modules/units/api/models/unit.model.ts"
import unitsService from "@/modules/units/api/services/units.service.ts"
import { reactive } from "vue"
import { ProgressiveImage } from "vue-progressive-image"

let state = reactive({ units: [] as Unit[] })
</script>

<style lang="scss" scoped>
.header {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12px;

  h4 {
    margin: 0;
  }

  .count {
    background-color: var(--kt-content-primary);
    width: 20px;
    height: 20px;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    user-select: none;
    color: #fff;
    font-family: var(--kt-headline-font), serif;
    font-size: 14px;
    font-weight: bold;
    border-radius: 4px;
  }
}

.serverList {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 1.2rem;

  .serverListItem {
    display: flex;
    flex-direction: row;
    gap: 16px;
    padding: 8px;
    border: 2px solid var(--kt-border-low);
    transition:
      border ease-in-out 0.15s,
      background-color ease-in-out 0.15s;
    border-radius: 20px;
    text-decoration: none;

    &:hover {
      cursor: pointer;
      border-color: var(--kt-border-medium);

      .title,
      .description {
        opacity: 1;
      }
    }

    &.selected {
      border-color: var(--kt-content-primary);
    }

    .image {
      width: 72px;
      height: 72px;
      min-width: 72px;
      min-height: 72px;
      border-radius: 16px;
      display: block;
      background-position: center;
      background-size: cover;
      background-repeat: no-repeat;

      :deep(img) {
        position: relative;
      }
    }

    .body {
      display: flex;
      flex-direction: column;
      padding: 8px 0;
      justify-content: center;
      gap: 0;
    }

    .title,
    .description {
      opacity: 0.88;
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
  }
}
</style>
