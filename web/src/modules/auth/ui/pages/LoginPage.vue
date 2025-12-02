<template>
  <AuthLayout>
    <h4>Log In</h4>
    <p :class="$style.subtitle">
      Enter your credentials to access your account.
    </p>
    <VAlert
      v-if="errorTranslationText"
      :class="$style.alert"
      variant="error"
    >
      <template #description>
        Deu ruim (errorTranslationText)
      </template>
    </VAlert>
    <VForm
      @submit.prevent="performLogin"
    >
      <VFieldSet>
        <VLabel>
          Email
          <VInput
            v-model="credentials.email"
            type="text"
            required="true"
            autocomplete="email"
          />
        </VLabel>
        <VLabel>
          Password
          <VInput
            v-model="credentials.password"
            type="password"
            required="true"
            autocomplete="current-password"
          />
        </VLabel>
      </VFieldSet>
      <VLayout gap="sm">
        <VButton
          type="submit"
          variant="primary"
          block
          :disabled="loginBeingPerformed"
          :class="$style.loginButton"
        >
          Log In
        </VButton>
        <VButton
          v-if="currentAccountName"
          variant="default"
          block
          @click="navigateToIndex"
        >
          Continue as Natan
        </VButton>
      </VLayout>
    </VForm>
  </AuthLayout>
</template>
<script setup lang="ts">
import VButton from "@/modules/platform/ui/components/button/VButton.vue"
import VForm from "@/modules/platform/ui/components/form/VForm.vue"
import VLabel from "@/modules/platform/ui/components/form/VLabel.vue"
import VFieldSet from "@/modules/platform/ui/components/form/VFieldSet.vue"
import VAlert from "@/modules/platform/ui/components/alert/VAlert.vue"
import AuthLayout from "@/modules/auth/ui/layouts/AuthLayout.vue"
import VInput from "@/modules/platform/ui/components/form/VInput.vue"
import { reactive, ref } from "vue"
import authService from "@/modules/auth/api/services/auth.service"
import type { HttpError } from "@/modules/platform/api/models/error.model"
import { useRouter } from "vue-router"
import { useAccountsStore } from "@/modules/accounts/accounts.store"
import VLayout from "@/modules/platform/ui/components/grid/VLayout.vue"

const router = useRouter()

// State
const credentials = reactive({ email: "", password: "" })
const errorTranslationText = ref<string | null>(null)
const loginBeingPerformed = ref<boolean>(false)
const currentAccountName = useAccountsStore().account?.username

// Functions
function performLogin() {
    if (loginBeingPerformed.value) return;

    loginBeingPerformed.value = true;
    errorTranslationText.value = null;

    authService
        .login(credentials.email, credentials.password)
        .then(navigateToIndex)
        .catch((error: HttpError) => {
            errorTranslationText.value =
                error.code === "ERR_NETWORK"
                    ? "auth.login.network-error"
                    : `error.${error.code}`;
        })
        .finally(() => {
            loginBeingPerformed.value = false;
        });
}

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

</style>
