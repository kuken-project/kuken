<script lang="ts" setup>
import blueprintsService from "@/modules/blueprints/api/services/blueprints.service.ts"
import Resource from "@/modules/platform/ui/components/Resource.vue"
import { reactive, ref, unref } from "vue"
import type { Blueprint } from "@/modules/blueprints/api/models/blueprint.model.ts"
import { isNull } from "@/utils"

const selected = ref<string | null>("")
const emit = defineEmits<{ selected: [blueprintId: string] }>()

const resource = () => blueprintsService.listReadyToUseBlueprints()
const { blueprintList } = reactive<{ blueprintList: Blueprint[] }>({ blueprintList: [] })

function isSelected(blueprint: Blueprint) {
    return !isNull(selected) && unref(selected) === blueprint.id
}

function select(blueprint: Blueprint) {
    if (isSelected(blueprint)) {
        selected.value = null
        emit("selected", "")
    } else {
        selected.value = blueprint.id
        emit("selected", blueprint.id)
    }
}
</script>

<template>
    <h4>Select a blueprint</h4>
    <Resource :resource="resource" @loaded="(value: Blueprint[]) => (blueprintList = value)">
        <template v-if="blueprintList">
            <ul v-for="blueprint in blueprintList" :key="blueprint.id" class="blueprintList">
                <li
                    :class="{ selected: isSelected(blueprint) }"
                    class="blueprint"
                    @click="select(blueprint)"
                >
                    <div
                        :style="{ backgroundImage: `url(${blueprint.spec.remote.assets.iconUrl})` }"
                        class="image"
                    />
                    <div class="body">
                        <h5 class="title" v-text="blueprint.spec.name" />
                        <p class="description">
                            Version {{ blueprint.spec.version }} · Küken Official Blueprint
                        </p>
                    </div>
                </li>
            </ul>
        </template>
    </Resource>
</template>

<style lang="scss" scoped>
.blueprintList {
    margin-top: 8px;
}

.blueprint {
    display: flex;
    flex-direction: row;
    gap: 16px;
    padding: 12px;
    border: 2px solid transparent;
    transition:
        border ease-in-out 0.15s,
        background-color ease-in-out 0.15s;
    border-radius: 20px;

    &:hover {
        cursor: pointer;
        border: 2px solid var(--kt-border-low);
    }

    &.selected {
        border: 2px solid var(--kt-content-primary);
    }
}

.image {
    width: 72px;
    height: 72px;
    border-radius: 20px;
    display: block;
    background-position: center;
    background-size: cover;
    background-repeat: no-repeat;
}

.body {
    display: flex;
    flex-direction: column;
    padding: 8px 0;
    justify-content: center;
    gap: 0px;
}

.description {
    color: var(--kt-content-neutral);
}
</style>
