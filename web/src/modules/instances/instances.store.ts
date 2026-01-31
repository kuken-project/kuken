import { defineStore } from "pinia"
import { isNull } from "@/utils"
import type { Instance } from "@/modules/instances/api/models/instance.model.ts"

type InstancesStore = { instance: Instance | null }

export const useInstanceStore = defineStore("instance", {
    state: (): InstancesStore => ({ instance: null }),
    getters: {
        getInstance(): Instance {
            if (isNull(this.instance)) throw new Error("Missing instance information")

            return this.instance
        }
    },
    actions: {
        updateInstance(instance: Instance | null) {
            this.instance = instance
        },
        resetInstance() {
            this.updateInstance(null)
        }
    }
})
