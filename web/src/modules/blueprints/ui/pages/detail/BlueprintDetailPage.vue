<template>
    <Resource :resource="resource" @loaded="(value) => (blueprint = value)">
        <template v-if="blueprint">
            <VContainer>
                <VCol :size="5">
                    <section>
                        <div class="header">
                            <div
                                class="icon"
                                :style="{
                                    backgroundImage: `url(${blueprint.spec.remote.assets.iconUrl})`
                                }"
                            />
                            <div class="text">
                                <h4 class="title">{{ blueprint.spec.name }}</h4>
                                <p class="description">
                                    Version {{ blueprint.spec.version }} · Küken Official Blueprint
                                </p>
                            </div>
                        </div>
                    </section>
                </VCol>
                <VCol :size="7">
                    <section>
                        <div class="servers-using">
                            <span>There’s no servers using this blueprint.</span>
                        </div>
                    </section>
                </VCol>
            </VContainer>
            <VContainer>
                <VCol :size="5">
                    <section class="build-configuration">
                        <h4>Build Configuration</h4>
                        <VCard>
                            <pre>
                    {{ JSON.stringify(blueprint.spec.build, null, 2) }}
                  </pre
                            >
                        </VCard>
                    </section>
                </VCol>
                <VCol :size="7">
                    <section class="file-descriptor">
                        <h4>File Descriptor</h4>
                        <VCard>
                            <pre>
                    {{ JSON.stringify(blueprint.spec, null, 2) }}
                  </pre
                            >
                        </VCard>
                    </section>
                </VCol>
            </VContainer>
        </template>
    </Resource>
</template>
<script setup lang="ts">
import { ref } from "vue"
import type { Blueprint } from "@/modules/blueprints/api/models/blueprint.model"
import blueprintsService from "@/modules/blueprints/api/services/blueprints.service"
import Resource from "@/modules/platform/ui/components/Resource.vue"
import VCol from "@/modules/platform/ui/components/grid/VCol.vue"
import VContainer from "@/modules/platform/ui/components/grid/VContainer.vue"
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import VCard from "@/modules/platform/ui/components/card/VCard.vue"

const props = defineProps<{ blueprintId: string }>()

const resource = () => blueprintsService.getBlueprint(props.blueprintId)
const blueprint = ref<Blueprint | null>(null)
</script>
<style lang="scss" scoped>
.header {
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 16px;

    .icon {
        background-position: center;
        background-size: cover;
        background-repeat: no-repeat;
        width: 96px;
        height: 96px;
        border-radius: 8px;
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
    align-items: center;
    justify-content: center;
    color: var(--kt-content-neutral-low);
    border-radius: 20px;
    background-color: var(--kt-background-surface-high);
    padding: 4.8rem 2.4rem;
    width: 100%;
    font-size: 16px;
}

.build-configuration,
.file-descriptor {
    margin-top: 4.8rem;

    .card {
        margin-top: 1.2rem;
    }
}
</style>
