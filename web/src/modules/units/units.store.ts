import type { Unit } from "@/modules/units/api/models/unit.model.ts"
import { isNull } from "@/utils"
import { defineStore } from "pinia"

type UnitStore = { unit: Unit | null }

export const useUnitStore = defineStore("unit", {
  state: (): UnitStore => ({ unit: null }),
  getters: {
    getUnit(): Unit {
      if (isNull(this.unit)) throw new Error("Missing unit information")

      return this.unit
    }
  },
  actions: {
    updateUnit(unit: Unit | null) {
      this.unit = unit
    },
    resetUnit() {
      this.updateUnit(null)
    }
  }
})
