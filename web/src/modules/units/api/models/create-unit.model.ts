export type CreateUnitRequest = {
    name: string
    blueprint: string
    inputs: { [name: string]: string }
    env: { [key: string]: string }
}
