<template>
    <header :class="$style.root">
        <div :class="$style.logo">
            <img src="/img/icon-white-transparent.png" alt="Logo" />
        </div>
        <template v-if="$slots.default">
            <slot />
        </template>
    </header>
</template>

<style lang="scss" module>
.root {
    padding: 0 16px;
    display: flex;
    flex-direction: row;
    align-items: center;
    width: 100%;
}

.logo img {
    max-width: 72px;
}
</style>
