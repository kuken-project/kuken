<template>
    <VContainer class="container">
        <router-view />
    </VContainer>
</template>

<script setup lang="ts">
import VContainer from "@/modules/platform/ui/components/grid/VContainer.vue"
</script>

<style scoped lang="scss">
.container {
    padding: 48px;
    display: flex;
    flex-direction: column;
}
</style>
