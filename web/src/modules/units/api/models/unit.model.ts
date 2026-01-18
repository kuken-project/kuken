import type { Blueprint } from "@/modules/blueprints/api/models/blueprint.model.ts"

export type Unit = {
    id: string
    name: string
    instance: UnitInstance
    created: string
    updated: string
    status: string
}

export type UnitInstance = {
    id: string
    address: UnitInstanceAddress
    blueprint: Blueprint
}

export type UnitInstanceAddress = {
    host: string
    port: string
}
