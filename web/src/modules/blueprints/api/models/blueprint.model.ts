export type Blueprint = {
  id: string
  header: BlueprintHeader
  official: boolean
}

export type BlueprintHeader = {
  name: string
  version: string
  author: string
  url: string
  icon: string
}

export function iconAsBase64PNG(icon: string): string {
  return `data:image/png;base64,${icon}`
}
