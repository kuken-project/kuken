<script lang="ts" setup>
import VContainer from "@/modules/platform/ui/components/grid/VContainer.vue"
import { computed } from "vue"
import { useRoute } from "vue-router"

type Link = { title: PropertyKey; route: string }

const route = useRoute()
const params = Object.entries(route.params)
const links = computed(() =>
  route.matched
    .filter((route) => {
      const title = route.meta.title
      return title != null && String(title).length > 0
    })
    .map((route) => {
      let path = route.path
      for (const [name, value] of params) {
        path = path.replace(`:${name}`, value)
      }

      return {
        title: String(route.meta.title),
        route: path
      } as Link
    })
)

const isVisible = computed(() => links.value.length > 1)
const inactiveLinks = computed(() => links.value.slice(0, -1))
const activeLink = computed(() => links.value[links.value.length - 1])
</script>

<template>
  <transition name="breadcrumb-container">
    <VContainer v-if="isVisible" class="breadcrumb-wrapper">
      <transition-group class="breadcrumb" name="breadcrumb" tag="div">
        <router-link
          v-for="link in inactiveLinks"
          :key="link.title"
          :to="{ path: link.route }"
          class="link"
        >
          {{ link.title }}
        </router-link>
        <span key="active" class="link" v-text="activeLink?.title" />
      </transition-group>
    </VContainer>
  </transition>
</template>

<style lang="scss" scoped>
.breadcrumb-wrapper {
  padding-top: 4.8rem;
  padding-bottom: 2.4rem;
  position: relative;
  z-index: 1;
}

.breadcrumb {
  display: flex;
  align-items: center;
  height: 24px;

  .link {
    font-size: 16px;
    color: var(--kt-content-neutral-high);
    user-select: none;
    display: inline-flex;
    align-items: center;
    height: 24px;
    line-height: 1;
    will-change: transform, opacity;
    backface-visibility: hidden;
    -webkit-font-smoothing: subpixel-antialiased;
  }

  a.link {
    text-decoration: none;
    color: var(--kt-content-neutral);

    &::after {
      content: "/";
      font-weight: bold;
      display: inline-block;
      padding: 0 12px;
      font-size: 12px;
      opacity: 0.38;
      align-self: center;
      backface-visibility: hidden;
    }
  }
}

.breadcrumb-container-enter-active,
.breadcrumb-container-leave-active {
  transition: all 0.1s ease;
}

.breadcrumb-container-enter-from {
  opacity: 0;
  transform: translateY(10px);
  position: absolute;
}

.breadcrumb-container-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.breadcrumb-enter-active {
  transition:
    opacity 0.3s ease,
    transform 0.3s ease;
}

.breadcrumb-enter-from {
  opacity: 0;
  transform: translateX(-20px) translateZ(0); // força GPU
}

.breadcrumb-leave-active {
  transition:
    opacity 0.3s ease,
    transform 0.3s ease;
  position: absolute;
}

.breadcrumb-leave-to {
  opacity: 0;
  transform: translateX(-20px) translateZ(0);
}

.breadcrumb-move {
  transition: transform 0.3s ease;
}
</style>
