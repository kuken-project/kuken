package gg.kuken.feature.setup

import gg.kuken.feature.account.AccountService
import gg.kuken.feature.remoteConfig.RemoteConfig
import gg.kuken.feature.remoteConfig.RemoteConfigService
import gg.kuken.feature.setup.http.dto.SetupRequest
import gg.kuken.feature.setup.model.SetupState
import gg.kuken.feature.setup.model.SetupState.Step
import kotlinx.coroutines.coroutineScope

class SetupService(
    private val accountService: AccountService,
    private val remoteConfigService: RemoteConfigService,
) {
    val requiredSetupSteps = linkedSetOf(Step.CreateAccount, Step.OrganizationName)

    suspend fun currentState(): SetupState {
        val stepsToComplete =
            coroutineScope {
                retrieveRemainingSteps()
            }

        return SetupState(
            completed = stepsToComplete.isEmpty(),
            remainingSteps = stepsToComplete.filter { step -> step in requiredSetupSteps }.toSet(),
        )
    }

    private suspend fun retrieveRemainingSteps(): Set<Step> =
        buildSet {
            if (!accountService.existsAnyAccount()) {
                add(Step.CreateAccount)
            }

            if (!remoteConfigService.isConfigValueSet(RemoteConfig.OrganizationName)) {
                add(Step.OrganizationName)
            }
        }

    suspend fun tryComplete(request: SetupRequest): SetupState {
        coroutineScope {
            remoteConfigService.setConfigValue(
                key = RemoteConfig.OrganizationName,
                value = request.organizationName,
            )

            accountService.createAccount(
                email = request.account.email,
                password = request.account.password,
            )
        }

        return SetupState(
            completed = true,
            remainingSteps = emptySet(),
        )
    }
}
