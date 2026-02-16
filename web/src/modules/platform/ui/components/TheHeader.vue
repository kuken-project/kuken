<script lang="ts" setup>
import { useAccount, useAccountsStore } from "@/modules/accounts/accounts.store.ts"
import VButton from "@/modules/platform/ui/components/button/VButton.vue"

const { isLoggedIn } = useAccountsStore()
let username: string
let isAdmin: boolean

if (isLoggedIn) {
  const account = useAccount().value
  username = account.email.charAt(0).toUpperCase()
  isAdmin = account.permissions.includes("account.manage")
}
</script>
<template>
  <header>
    <router-link class="logo" :to="{ path: '/' }">
      <img alt="Logo" src="/img/icon-white-transparent.png" />
    </router-link>
    <template v-if="$slots.default">
      <slot />
    </template>
    <div v-if="isLoggedIn" class="profile">
      <div class="avatar">
        {{ username }}
      </div>
    </div>
    <div class="create-button">
      <VButton variant="primary" :to="{ name: 'units.create' }">Create new server</VButton>
      <VButton v-if="isAdmin" variant="primary" :to="{ name: 'organization' }">Org</VButton>
    </div>
  </header>
</template>

<style lang="scss" scoped>
header {
  padding: 0 16px;
  display: flex;
  flex-direction: row;
  align-items: center;
  width: 100%;

  .logo img {
    max-width: 72px;
    user-select: none;
  }

  .avatar {
    width: 64px;
    height: 64px;
    border-radius: 50%;
  }
}
</style>
