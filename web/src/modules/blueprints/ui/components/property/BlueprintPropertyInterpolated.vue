<script lang="ts" setup>
import type {
  BlueprintProperty,
  InterpolatedBlueprintProperty
} from "@/modules/blueprints/api/models/blueprint.spec.model.ts"
import BlueprintPropertyInterpolatedElement from "@/modules/blueprints/ui/components/property/BlueprintPropertyInterpolatedElement.vue"
import { isUndefined } from "@/utils"
import { computed, readonly } from "vue"

const props = defineProps<{
  property: InterpolatedBlueprintProperty
  inputValues: { [name: string]: string }
}>()

const original = readonly(props.property)
const emit = defineEmits<{
  "update:property": [value: typeof props.property]
  "removed:property": [value: (typeof props.property.parts)[number]]
}>()

const refTranslations = {
  "network.port": "Server port"
}

const htmlContent = computed(() => {
  let result = []
  for (const part of props.property.parts) {
    if (part.type === "literal") {
      if (part.value.trim().length > 0) {
        result.push(`<code data-el-type="${part.type}">${part.value}</code>`)
      }
    }

    if (part.type === "conditional") {
      const inputValue = props.inputValues[part.inputName!]
      console.log("conditional updated", part.inputName, inputValue)

      if (!isUndefined(inputValue) && inputValue === "true") {
        console.log(`  - [ok]: ${part.inputName}`)
        result.push(
          `<code class="element conditional" contenteditable="false" data-el-type="${part.type}" data-input-name="${part.inputName}">${part.value}</code>`
        )
      } else {
        console.log(`  - [not ok]: ${part.inputName}`)
      }
    }

    if (part.type === "ref") {
      result.push(
        `<code class="element ref" contenteditable="false" data-el-type="${part.type}" data-ref="${part.refPath}">${refTranslations[part.refPath!]}</code>`
      )
    }
  }
  return result
})

const updateContent = (event: InputEvent) => {
  const children = (event.target as HTMLPreElement).children
    .item(0)!
    .querySelectorAll("[data-el-type]")
  console.log("children", children)

  const types: BlueprintProperty[] = []

  for (const child of children) {
    const type = child.getAttribute("data-el-type")! as BlueprintProperty["type"]

    switch (type) {
      case "literal": {
        types.push({
          type: type,
          value: child.innerHTML
        })
        break
      }
      case "ref": {
        types.push({
          type: type,
          refPath: child.getAttribute("data-ref") as string
        })
        break
      }
      case "conditional": {
        types.push({
          type: type,
          inputName: child.getAttribute("data-input-name") as string,
          value: child.innerHTML
        })
        break
      }
    }
  }
  console.log("types", types)

  const updated: InterpolatedBlueprintProperty = { ...props.property, parts: types }
  console.log("Original property", original)
  console.log("Updated property", updated)

  emit("update:property", updated)
}
</script>

<template>
  <div class="wrapper" contenteditable="true" @input="updateContent">
    <pre
      class="code"
    ><BlueprintPropertyInterpolatedElement v-for="el in htmlContent" :html="el"/></pre>
  </div>
</template>

<style lang="scss">
.element {
  border-radius: 4px;
  padding: 1px 4px;
  margin: 0 2px;
  line-height: 1.6;
  font-family: "JetBrains Mono", "Consolas", "Monaco", "Courier New", monospace;
  user-select: none;

  &.conditional {
    background-color: rgba(75, 123, 236, 0.18);
    color: rgba(75, 123, 236, 1);
  }

  &.ref {
    background-color: rgba(165, 94, 234, 0.18);
    color: rgba(165, 94, 234, 1);
  }
}

.wrapper {
  outline: none;
}

.code {
  border: 1.5px solid var(--kt-border-medium);
  border-radius: 8px;
  font-weight: 400;
  text-wrap: balance;
  padding: 8px 12px;

  &:hover {
    border-color: var(--kt-border-low);
  }
}

.breadcrumb-enter-active,
.breadcrumb-leave-active {
  transition: all 0.1s ease;
}

.breadcrumb-enter-from {
  opacity: 0;
  transform: translateY(10px);
  position: absolute;
}

.breadcrumb-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
