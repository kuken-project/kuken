export type BackendInfo = {
    production: boolean
    version: string
    organization: BackendOrganizationInfo
}

export type BackendOrganizationInfo = {
    name: string
}
