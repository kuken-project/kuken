<template>
  <div class="root">
    <div class="left_side">
      <TheLogo
        black
        class="logo"
      />
      <div class="content">
        <slot />
      </div>
    </div>
    <div class="right_side" />
  </div>
</template>
<script setup lang="ts">
import TheLogo from "@/modules/platform/ui/components/TheLogo.vue"
</script>
<style lang="scss" scoped>
// TODO remove it from here
@import "@/assets/styles/abstracts/variables";

.root {
	display: flex;
	align-items: center;
	height: 100%;
}

.container {
	display: flex;
	align-items: center;
	height: 100%;
}

.left_side {
	align-items: normal;
	z-index: 1;
	background: var(--kt-background-body);
	position: relative;
	left: 0;
	height: 100%;
	width: 30%;
	max-width: 30%;
	box-shadow: RGB(0 0 0 / 24%) 4px 0 8px 0;
	padding: 4.8rem 7.2rem;

	@media (max-width: map-get($breakpoints, "medium")) {
		& {
			width: 100%;
			max-width: 100%;
			padding: 24px;
		}
	}

	.logo {
		max-width: 64px;
		position: relative;
		border-radius: 8px;
	}

	.content {
		margin-top: 96px;
	}
}

.right_side {
	position: relative;
	flex: 1;
	display: flex;
	height: 100%;
	flex-direction: row;
	align-items: flex-end;
	overflow: hidden;
	background-size: cover;
	background-repeat: no-repeat;
	background-position: center;
	background-image: url("@/assets/media/illustrations/2601286834.jpg");
	padding: 24px 48px;

	&:hover .game_name,
	&:hover .version_info {
		opacity: 1;
	}

	@media (max-width: map-get($breakpoints, "medium")) {
		& {
			display: none;
		}
	}
}
</style>