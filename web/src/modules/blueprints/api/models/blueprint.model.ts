export type Blueprint = {
    id: string
    spec: BlueprintSpec
}

export type BlueprintSpec = {
    name: string
    version: string
    remote: BlueprintRemote
}

export type BlueprintRemote = {
    assets: BlueprintRemoteAssets
}

export type BlueprintRemoteAssets = {
    iconUrl: string
}