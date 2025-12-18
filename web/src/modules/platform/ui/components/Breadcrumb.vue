<script setup lang="ts">
import { useRoute } from "vue-router"
import { computed } from "vue"

type Link = { title: PropertyKey; route: string }

const route = useRoute()
const links = computed(() =>
    route.matched
        .filter((route) => {
            const title = route.meta.title
            return title != null && String(title).length > 0
        })
        .map((route) => {
            return {
                title: String(route.meta.title),
                route: route.path
            } as Link
        })
)

const isVisible = computed(() => links.value.length > 1)
const inactiveLinks = computed(() => links.value.slice(0, -1))
const activeLink = computed(() => links.value[links.value.length - 1])
</script>

<template>
    <div v-if="isVisible" class="root">
        <router-link
            v-for="link in inactiveLinks"
            :key="link.title"
            class="link"
            :to="{ path: link.route }"
        >
            {{ link.title }}
        </router-link>
        <span key="active" class="link" v-text="activeLink?.title" />
    </div>
</template>

<style scoped lang="scss">
.root {
    margin-bottom: 2rem;

    .link {
        font-size: 16px;
        color: var(--kt-content-neutral-high);
        user-select: none;
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
        }
    }
}
</style>
