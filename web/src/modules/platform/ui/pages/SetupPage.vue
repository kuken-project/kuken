<template>
  <div class="header">
    <VContainer class="items">
      <TheLogo
        black
        class="logo"
      />
      <ul class="links">
        <li>
          <a
            :href="links.docs"
            target="_blank"
          >setup.header.label.docs</a>
        </li>
        <li>
          <a
            :href="links.login"
          >setup.header.label.login</a>
        </li>
      </ul>
    </VContainer>
  </div>
  <div class="root">
    <VContainer>
      <h2
        class="title"
      >setup.title'</h2>
      <h4>Let's get you up and running!</h4>
    </VContainer>
  </div>
</template>

<script setup lang="ts">
import VContainer from "@/modules/platform/ui/components/grid/VContainer.vue"
import TheLogo from "@/modules/platform/ui/components/TheLogo.vue"
import configService from "@/modules/platform/api/services/config.service"
import { useRouter } from "vue-router"

const router = useRouter()
const links = {
    docs: configService.appWebsite,
    login: "/login"
}
</script>

<style scoped lang="scss">
.root {
    background-color: var(--kt-background-surface);
    height: 100%;
    width: 100%;
}

.title {
    margin-top: 0.8em;
    user-select: none;
}

.header {
    background-color: var(--kt-background-surface);
    border-bottom: 1px solid var(--kt-border-medium);
    padding: 0.8rem 0;

    .items {
        display: flex;
        flex-direction: row;
        align-content: space-between;
        width: 100%;
    }

    .logo {
        width: 48px;
        height: 48px;
        border-radius: 8px;
    }

    .links {
        display: flex;
        align-items: center;
        justify-content: flex-end;
        flex-grow: 1;
        gap: 2.4rem;

        li {
            font-weight: 500;
            font-size: 14px;

            a {
                text-decoration: none;
                color: var(--kt-content-neutral);

                &:hover {
                    color: var(--kt-content-neutral-low);
                }
            }
        }
    }
}

.languages {

}
</style>