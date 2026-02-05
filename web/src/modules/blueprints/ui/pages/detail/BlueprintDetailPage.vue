<template>
  <Resource :resource="resource" @loaded="(value: Blueprint) => (blueprint = value)">
    <template v-if="blueprint">
      <VContainer>
        <VCol :size="5">
          <section>
            <div class="header">
              <div
                :style="{
                  backgroundImage: `url(${resolveBlueprintSource(blueprint.header.assets?.icon)})`
                }"
                class="icon"
              />
              <div class="text">
                <h4 class="title">{{ blueprint.header.name }}</h4>
                <p class="description">
                  Version {{ blueprint.header.version }}
                  {{ blueprint.official ? " · Küken Official Blueprint" : "" }}
                </p>
              </div>
            </div>
          </section>
        </VCol>
        <VCol :size="7">
          <section>
            <div class="servers-using">
              <span>There’s no servers using this blueprint.</span>
              <VButton
                :to="{
                  name: 'units.create',
                  params: { blueprint: blueprint.id }
                }"
                variant="primary"
              >
                Create new server
              </VButton>
            </div>
          </section>
        </VCol>
      </VContainer>
      <VContainer>
        <VCol :size="5">
          <section class="build-configuration">
            <h4>Build Configuration</h4>
            <VCard style="padding: 0">
              <!--                            <higlight-->
              <!--                                :autodetect="false"-->
              <!--                                :code="JSON.stringify(blueprint.spec.build, null, 2)"-->
              <!--                                class="highlight"-->
              <!--                                language="json"-->
              <!--                            />-->
            </VCard>
          </section>
        </VCol>
        <VCol :size="7">
          <section class="file-descriptor">
            <h4>File Descriptor</h4>
            <VCard style="padding: 0">
              <!--                            <higlight-->
              <!--                                :autodetect="false"-->
              <!--                                :code="JSON.stringify(blueprint.spec, null, 2)"-->
              <!--                                class="highlight"-->
              <!--                                language="json"-->
              <!--                            />-->
            </VCard>
          </section>
        </VCol>
      </VContainer>
    </template>
  </Resource>
</template>
<script lang="ts" setup>
import {
  type Blueprint,
  resolveBlueprintSource
} from "@/modules/blueprints/api/models/blueprint.model"
import blueprintsService from "@/modules/blueprints/api/services/blueprints.service"
import Resource from "@/modules/platform/ui/components/Resource.vue"
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import VCard from "@/modules/platform/ui/components/card/VCard.vue"
import VCol from "@/modules/platform/ui/components/grid/VCol.vue"
import VContainer from "@/modules/platform/ui/components/grid/VContainer.vue"
import hljsVuePlugin from "@highlightjs/vue-plugin"
import { useHead } from "@unhead/vue"
import { ref } from "vue"

const higlight = hljsVuePlugin.component
const props = defineProps<{ blueprintId: string }>()

const resource = () => blueprintsService.getBlueprint(props.blueprintId)
const blueprint = ref<Blueprint | null>(null)

useHead({
  title: () => (blueprint.value ? `${blueprint.value.header.name} - Game Directory` : "")
})
</script>
<style lang="scss">
pre code.hljs {
  text-wrap: auto;
  border-radius: 20px !important;
}
</style>
<style lang="scss" scoped>
.header {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16px;

  .icon {
    background-size: cover;
    background-repeat: no-repeat;
    width: 96px;
    height: 96px;
    border-radius: 20px;
  }

  .text {
    display: flex;
    flex-direction: column;
    justify-content: center;

    .description {
      color: var(--kt-content-neutral);
    }
  }
}

.servers-using {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--kt-content-neutral-low);
  border-radius: 20px;
  background-color: var(--kt-background-surface-high);
  padding: 4.8rem 2.4rem;
  width: 100%;
  font-size: 16px;

  span {
    user-select: none;
    margin-bottom: 0.8rem;
  }
}

.build-configuration,
.file-descriptor {
  margin-top: 4.8rem;

  .card {
    margin-top: 1.2rem;
  }
}
</style>
