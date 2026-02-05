<template>
  <AuthLayout>
    <h4>Log In</h4>
    <p :class="$style.subtitle">
      <template v-if="organizationName">
        Enter your credentials to access {{ organizationName }}.
      </template>
      <template v-else> Enter your credentials to access your account. </template>
    </p>
    <VForm @submit.prevent="performLogin">
      <VFieldSet>
        <VLabel>
          Email
          <VInput v-model="credentials.email" autocomplete="email" required="true" type="email" />
        </VLabel>
        <VLabel>
          Password
          <VInput
            v-model="credentials.password"
            autocomplete="current-password"
            required="true"
            type="password"
          />
        </VLabel>
      </VFieldSet>
      <p v-if="errorTranslationText" :class="$style.error" v-text="errorTranslationText" />
      <VLayout gap="sm">
        <VButton
          :class="$style.loginButton"
          :disabled="loginBeingPerformed"
          block
          type="submit"
          variant="primary"
        >
          Log In
        </VButton>
        <VButton v-if="currentAccount" block variant="default" @click="navigateToIndex">
          Continue as {{ currentAccount }}
        </VButton>
      </VLayout>
    </VForm>
  </AuthLayout>
</template>
<script lang="ts" setup>
import { useAccountsStore } from "@/modules/accounts/accounts.store"
import authService from "@/modules/auth/api/services/auth.service"
import AuthLayout from "@/modules/auth/ui/layouts/AuthLayout.vue"
import type { HttpError } from "@/modules/platform/api/models/error.model"
import { usePlatformStore } from "@/modules/platform/platform.store.ts"
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import VFieldSet from "@/modules/platform/ui/components/form/VFieldSet.vue"
import VForm from "@/modules/platform/ui/components/form/VForm.vue"
import VInput from "@/modules/platform/ui/components/form/VInput.vue"
import VLabel from "@/modules/platform/ui/components/form/VLabel.vue"
import VLayout from "@/modules/platform/ui/components/grid/VLayout.vue"
import { useHead } from "@unhead/vue"
import { reactive, ref } from "vue"
import { useRouter } from "vue-router"

const organizationName = usePlatformStore().getBackendInfo.organization.name

useHead({
  title: `Login to ${organizationName}`,
  titleTemplate: null
})

const credentials = reactive({ email: "", password: "" })
const errorTranslationText = ref<string | null>(null)
const loginBeingPerformed = ref<boolean>(false)
const currentAccount = useAccountsStore().account?.email

function performLogin() {
  if (loginBeingPerformed.value) return

  loginBeingPerformed.value = true
  errorTranslationText.value = null

  authService
    .login(credentials.email, credentials.password)
    .then(navigateToIndex)
    .catch((error: HttpError) => {
      if (error.code === "ERR_NETWORK")
        errorTranslationText.value = "Unable to connect to the authentication server."

      if (error.code === 1001) errorTranslationText.value = "Invalid username or password."
      else errorTranslationText.value = `Unknown error (code ${error.code})`
    })
    .finally(() => {
      loginBeingPerformed.value = false
    })
}

const router = useRouter()
function navigateToIndex() {
  router.push("/")
}
</script>
<style lang="scss" module>
h4 {
  margin-bottom: 0.8rem;
  user-select: none;
}

form {
  padding-top: 0.8rem;
}

.loginButton {
  margin-top: var(--space-lg);
}

.alert {
  margin-bottom: 2.4rem !important;
}

.subtitle {
  margin-top: 0.4rem;
  color: var(--kt-content-neutral);
  margin-bottom: 3.6rem;
  user-select: none;
}

.error {
  color: var(--kt-content-negative);
  margin-top: -0.8rem;
}
</style>
