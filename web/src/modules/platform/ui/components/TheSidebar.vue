<template>
  <aside
    :class="$style.root"
    role="complementary"
  >
    <div :class="$style.logo">
      <router-link to="/">
        <TheLogo :black="true" />
      </router-link>
    </div>
    <div :class="$style.items">
      <ul>
        <li>
          <router-link to="about">
            <!--            <VIcon name="Information" />-->
          </router-link>
        </li>
        <li>
          <a href="#">
            <VIcon name="Apps" />
          </a>
        </li>
        <li>
          <router-link to="/blueprints">
            <VIcon name="Puzzle" />
          </router-link>
        </li>
        <li>
          <router-link to="users">
            <!--            <VIcon name="AccountMultipleOutline" />-->
          </router-link>
        </li>
      </ul>
    </div>
    <div :class="$style.items">
      <ul>
        <li>
          <a
            :href="appWebsite"
            target="_blank"
          >
            <!--            <VIcon name="HelpCircleOutline" />-->
          </a>
        </li>
        <li>
          <router-link
            to="about"
            :class="$style.account"
          >
            <Avatar
              :class="$style.accountAvatar"
              :src="undefined"
              label="Account avatar"
            />
          </router-link>
        </li>
      </ul>
    </div>
  </aside>
</template>
<script setup lang="ts">
import Avatar from "@/modules/platform/ui/components/Avatar.vue"
import TheLogo from "@/modules/platform/ui/components/TheLogo.vue"
import VIcon from "@/modules/platform/ui/components/icons/VIcon.vue"
</script>
<style lang="scss" module>
.root {
  padding: 1.6rem 0;
  display: flex;
  flex-direction: column;
  background-color: var(--kt-background-surface);
  border-right: 1px solid var(--kt-border-low);
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 4px;
  padding: 0 12px;

  img {
    width: 48px;
    height: 48px;
    border-radius: 8px;
  }
}

.items {
  flex-grow: 1;
  margin-top: 24px;
  display: flex;
  justify-content: center;
  align-items: baseline;

  &:last-child {
    align-items: flex-end;
  }

  ul li {
    display: flex;
    justify-content: center;

    &:not(:last-child) {
      margin-bottom: 16px;
    }

    a {
      padding: 6px;
      border-radius: 8px;
      color: var(--kt-content-neutral-high);

      &:hover {
        background-color: var(--kt-content-neutral-overlay);
      }

      :deep(svg) {
        width: 24px;
        height: 24px;
      }
    }
  }
}

.account {
}

.accountAvatar {
  max-height: 24px;
}
</style>
