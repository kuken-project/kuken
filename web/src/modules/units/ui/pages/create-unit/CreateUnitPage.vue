<script lang="ts" setup>
import unitsService from "@/modules/units/api/services/units.service.ts"
import { useAsyncState } from "@vueuse/core"
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import { computed, reactive, unref } from "vue"
import VForm from "@/modules/platform/ui/components/form/VForm.vue"
import VContainer from "@/modules/platform/ui/components/grid/VContainer.vue"
import VTitle from "@/modules/platform/ui/components/typography/VTitle.vue"
import Breadcrumb from "@/modules/platform/ui/components/Breadcrumb.vue"
import CreateUnitNameInput from "@/modules/units/ui/components/create-unit/CreateUnitNameInput.vue"
import CreateUnitBlueprintSelector from "@/modules/units/ui/components/create-unit/CreateUnitBlueprintSelector.vue"
import { useRouter } from "vue-router"
import type { Unit } from "@/modules/units/api/models/unit.model.ts"

const form = reactive({
    name: "",
    blueprint: "",
    image: "itzg/minecraft-server:latest"
})

const router = useRouter()
const { isLoading, execute } = useAsyncState(unitsService.createUnit, null as unknown as Unit, {
    immediate: false,
    onSuccess: (payload: Unit) => {
        window.location.href = router.resolve({
            name: "instance.console",
            params: { instanceId: payload.instance.id }
        }).href
    }
})

const steps = reactive({
    current: "name",
    all: ["name", "blueprint"]
})

const lastStep = computed(() => steps.all[steps.all.length - 1])

const buttonLabel = computed(() => {
    return steps.current === unref(lastStep) ? "Create" : "Next"
})

const canProceed = computed(() => {
    console.log(unref(isLoading))
    console.log(steps.current)
    if (unref(isLoading)) return false

    if (steps.current == "blueprint") {
        return form.blueprint.length > 0
    }

    return true
})

function proceed() {
    if (steps.current === unref(lastStep)) {
        execute(0, {
            name: form.name,
            blueprint: form.blueprint,
            image: form.image
        })
    } else {
        steps.current = steps.all[steps.all.indexOf(steps.current) + 1]!
    }
}
</script>

<template>
    <VContainer class="container">
        <Breadcrumb />
        <VTitle :centered="true">Create new server</VTitle>
        <div class="content">
            <VForm @submit.prevent="proceed">
                <CreateUnitNameInput v-if="steps.current == 'name'" v-model="form.name" />
                <CreateUnitBlueprintSelector
                    v-if="steps.current == 'blueprint'"
                    @selected="(blueprintId: string) => (form.blueprint = blueprintId)"
                />
                <VButton
                    :disabled="!canProceed"
                    type="submit"
                    variant="primary"
                    v-text="buttonLabel"
                />
            </VForm>
        </div>
    </VContainer>
</template>

<style lang="scss" scoped>
.container {
    padding: 48px;
    display: flex;
    flex-direction: column;
}

.content {
    display: flex;
    flex-direction: column;
    max-width: 40%;
    position: relative;
    left: 50%;
    transform: translateX(-50%);
}

.button {
    margin-top: 4.8rem;
    width: 15%;
    align-self: flex-end;
}
</style>
