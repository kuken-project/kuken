<template>
  <header :class="$style.root">
    <div :class="$style.account_info">
      <router-link
        to="account"
        title="My Account"
      >
        <Avatar
          :src="undefined"
          :label="`Account avatar`"
        />
      </router-link>
    </div>
  </header>
</template>
<script setup lang="ts">
import Avatar from "@/modules/platform/ui/components/Avatar.vue"
</script>
<style lang="scss" module>
.root {
	position: absolute;
	right: 0;
	margin-top: 3.2rem;
	margin-right: 4.8rem;
}

.account_info {
	width: 40px;
	height: 40px;
	max-height: 40px;
}
</style>