package mega.privacy.android.app.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mega.privacy.android.app.featuretoggle.AppFeatures
import mega.privacy.android.app.main.model.InviteContactState
import mega.privacy.android.domain.entity.Feature
import mega.privacy.android.domain.usecase.featureflag.GetFeatureFlagValueUseCase
import javax.inject.Inject

/**
 * InviteContact ViewModel
 */
@HiltViewModel
class InviteContactViewModel @Inject constructor(
    private val getFeatureFlagValueUseCase: GetFeatureFlagValueUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(InviteContactState())

    val state = _state.asStateFlow()

    init {
        getEnabledFeatures()
    }

    private fun getEnabledFeatures() {
        viewModelScope.launch {
            val enabledFeatures = setOfNotNull(
                AppFeatures.QRCodeCompose.takeIf { getFeatureFlagValueUseCase(it) }
            )
            _state.update { it.copy(enabledFeatureFlags = enabledFeatures) }
        }
    }

    /**
     * Check if given feature flag is enabled or not
     */
    fun isFeatureEnabled(feature: Feature) = state.value.enabledFeatureFlags.contains(feature)
}