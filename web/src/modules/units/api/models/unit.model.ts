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
    blueprint: UnitInstanceBlueprint
}

export type UnitInstanceAddress = {
    host: string
    port: string
}

export type UnitInstanceBlueprint = {
    id: string
    iconUrl?: string
}