<script lang="ts" setup>
import {
  type Blueprint,
  resolveBlueprintSource
} from "@/modules/blueprints/api/models/blueprint.model.ts"
import blueprintsService from "@/modules/blueprints/api/services/blueprints.service.ts"
import Resource from "@/modules/platform/ui/components/Resource.vue"
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import VFieldSet from "@/modules/platform/ui/components/form/VFieldSet.vue"
import VForm from "@/modules/platform/ui/components/form/VForm.vue"
import VInput from "@/modules/platform/ui/components/form/VInput.vue"
import VLabel from "@/modules/platform/ui/components/form/VLabel.vue"
import VCol from "@/modules/platform/ui/components/grid/VCol.vue"
import VContainer from "@/modules/platform/ui/components/grid/VContainer.vue"
import VLayout from "@/modules/platform/ui/components/grid/VLayout.vue"
import VTitle from "@/modules/platform/ui/components/typography/VTitle.vue"
import { reactive, ref } from "vue"

let state = reactive({ readyToUseBlueprints: [] as Blueprint[] })
const importUrl = ref("")
const importError = ref("")

function performImport() {
  blueprintsService
    .importBlueprint(importUrl.value)
    .then(() => window.location.reload())
    .catch((error) => {
      if (error.code === 1012 /* Validation error */) {
        importError.value = error.message
      }
    })
}
</script>

<template>
  <VContainer>
    <VTitle>Game Directory</VTitle>
    <VLayout gap="lg" direction="horizontal">
      <VCol :size="4">
        <Resource
          :resource="blueprintsService.listReadyToUseBlueprints"
          @loaded="(blueprints: Blueprint[]) => (state.readyToUseBlueprints = blueprints)"
        >
          <div class="header">
            <h4>Your Library</h4>
            <span class="blueprintsCount" v-text="state.readyToUseBlueprints.length" />
          </div>
          <p style="color: var(--kt-content-neutral)">
            Games that are available to create new servers immediately.
          </p>
          <div class="blueprintList">
            <router-link
              v-for="blueprint in state.readyToUseBlueprints"
              :key="blueprint.id"
              :to="{ name: 'blueprints.details', params: { blueprintId: blueprint.id } }"
              class="blueprint"
            >
              <div class="blueprintIcon">
                <img
                  :alt="`${blueprint.id} icon`"
                  :src="resolveBlueprintSource(blueprint.header.assets.icon)"
                />
              </div>
            </router-link>
            <div class="importBlueprint">+</div>
          </div>
        </Resource>
      </VCol>
      <VCol :size="8">
        <h4>Import from URL</h4>
        <div v-if="importError" class="importError">
          <pre><code>{{ importError }}</code></pre>
        </div>
        <VForm @submit.prevent>
          <VFieldSet>
            <VLabel>
              URL
              <VInput
                v-model="importUrl"
                placeholder="https://kuken.io"
                required="true"
                type="url"
              />
            </VLabel>
          </VFieldSet>
          <VButton variant="primary" @click="performImport">Import</VButton>
        </VForm>
      </VCol>
    </VLayout>
  </VContainer>
</template>

<style lang="scss" scoped>
.blueprintList {
  display: grid;
  gap: 12px;
  margin-top: 24px;
  grid-template-columns: 128px 128px 128px 128px;
}

.header {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12px;
}

form {
  margin-top: 40px;
}

.importBlueprint {
  width: 128px;
  height: 128px;
  position: relative;
  color: var(--kt-content-primary);
  background-color: #ffefe9;
  border-radius: 20px;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  font-size: 65px;
  font-family: var(--kt-headline-font), sans-serif;
  font-weight: lighter;
  user-select: none;
  cursor: pointer;
}

.blueprintsCount {
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
  transform: translateY(15%);
}

.blueprintIcon {
  max-width: 128px;
  max-height: 128px;
  position: relative;

  &:hover::before {
    opacity: 0.38;
    cursor: pointer;
  }

  &::before {
    content: "";
    width: 100%;
    height: 100%;
    position: absolute;
    background-color: #000;
    opacity: 0;
    border-radius: 20px;
    transition: opacity 75ms linear;
  }

  img {
    width: 100%;
    height: 100%;
    max-width: 128px;
    max-height: 128px;
    border-radius: 20px;
  }
}

.importError {
  border: 2px dashed var(--kt-background-surface-high);
  border-radius: 20px;
  background-color: var(--kt-content-negative);
  padding: 16px;
  margin-top: 8px;
  color: #fff;

  pre {
    text-wrap: wrap;
  }
}
</style>
