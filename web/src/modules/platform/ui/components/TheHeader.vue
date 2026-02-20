<script lang="ts" setup>
import { useAccount, useAccountsStore } from "@/modules/accounts/accounts.store.ts"
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import VLayout from "@/modules/platform/ui/components/grid/VLayout.vue"

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
    <VLayout direction="horizontal" gap="sm" :style="{ alignItems: 'center' }">
      <div class="create-button">
        <VButton variant="primary" :to="{ name: 'units.create' }">Create new server</VButton>
        <VButton v-if="isAdmin" variant="primary" :to="{ name: 'organization' }">Org</VButton>
      </div>
      <router-link :to="{ name: 'profile' }" v-if="isLoggedIn" class="profile">
        <div class="avatar">
          <span>{{ username }}</span>
        </div>
      </router-link>
    </VLayout>
  </header>
</template>

<style lang="scss" scoped>
header {
  padding: 0 16px;
  display: flex;
  flex-direction: row;
  align-items: center;
  width: 100%;
  justify-content: space-between;

  .logo img {
    max-width: 72px;
    user-select: none;
  }

  .profile {
    text-decoration: none;
  }

  .create-button [type="button"] {
    background-color: var(--kt-content-primary-oncolor-overlay);
    border-color: var(--kt-content-primary-oncolor-overlay);
  }

  .avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background-color: var(--kt-content-primary-oncolor-overlay);
    position: relative;
    display: flex;

    span {
      margin: auto;
      font-size: 21px;
      font-weight: bold;
      color: var(--kt-content-primary-oncolor);
    }
  }
}
</style>
