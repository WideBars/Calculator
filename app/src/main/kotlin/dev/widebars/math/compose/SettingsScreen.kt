package dev.widebars.math.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import dev.widebars.commons.compose.extensions.BooleanPreviewParameterProvider
import dev.widebars.commons.compose.extensions.MyDevices
import dev.widebars.commons.compose.lists.SimpleColumnScaffold
import dev.widebars.commons.compose.settings.SettingsGroup
import dev.widebars.commons.compose.settings.SettingsHorizontalDivider
import dev.widebars.commons.compose.settings.SettingsPreferenceComponent
import dev.widebars.commons.compose.settings.SettingsPurchaseComponent
import dev.widebars.commons.compose.settings.SettingsSwitchComponent
import dev.widebars.commons.compose.theme.AppThemeSurface
import dev.widebars.commons.helpers.isTiramisuPlus
import dev.widebars.math.BuildConfig
import dev.widebars.commons.R as CommonR
import dev.widebars.math.R
import dev.widebars.strings.R as StringsR

@Composable
internal fun SettingsScreen(
    goBack: () -> Unit,
    customizeColors: () -> Unit,
    customizeWidgetColors: () -> Unit,
    preventPhoneFromSleeping: Boolean,
    onPreventPhoneFromSleeping: (Boolean) -> Unit,
    vibrateOnButtonPressFlow: Boolean,
    onVibrateOnButtonPressFlow: (Boolean) -> Unit,
    isPro: Boolean,
    onPurchaseClick: () -> Unit,
    onAboutClick: () -> Unit,
    isUseEnglishEnabled: Boolean,
    isUseEnglishChecked: Boolean,
    onUseEnglishPress: (Boolean) -> Unit,
    onSetupLanguagePress: () -> Unit,
    showCheckmarksOnSwitches: Boolean,
    displayLanguage: String
) {
    SimpleColumnScaffold(
        title = stringResource(id = CommonR.string.settings),
        goBack = goBack
    ) {
        // Support project
        var shouldShake by remember { mutableStateOf(false) }
        if (!isPro) {
            SettingsPurchaseComponent(
                onPurchaseClick = onPurchaseClick,
                enabledShake = shouldShake,
                onShakeFinished = {
                    shouldShake = false
                }
            )
        }

        // Appearance
        SettingsGroup(title = {
            Text(text = stringResource(id = CommonR.string.color_customization).uppercase())
        }) {
            SettingsPreferenceComponent(
                label = stringResource(id = CommonR.string.customize_colors),
                doOnPreferenceClick = {
                    customizeColors()
                },
                preferenceLabelColor = MaterialTheme.colorScheme.onSurface,
                showChevron = true,
            )
            SettingsHorizontalDivider(thickness = 2.dp)
            SettingsPreferenceComponent(
                label = stringResource(id = CommonR.string.customize_widget_colors),
                doOnPreferenceClick = customizeWidgetColors,
                showChevron = true,
            )
        }

        // General
        SettingsGroup(title = {
            Text(text = stringResource(id = CommonR.string.general_settings).uppercase())
        }) {
            SettingsSwitchComponent(
                label = stringResource(id = R.string.vibrate_on_button_press),
                initialValue = vibrateOnButtonPressFlow,
                onChange = onVibrateOnButtonPressFlow,
                showCheckmark = showCheckmarksOnSwitches
            )
            SettingsHorizontalDivider(thickness = 2.dp)
            SettingsSwitchComponent(
                label = stringResource(id = CommonR.string.prevent_phone_from_sleeping),
                initialValue = preventPhoneFromSleeping,
                onChange = onPreventPhoneFromSleeping,
                showCheckmark = showCheckmarksOnSwitches
            )
            if (isUseEnglishEnabled) {
                SettingsHorizontalDivider(thickness = 2.dp)
                SettingsSwitchComponent(
                    label = stringResource(id = CommonR.string.use_english_language),
                    initialValue = isUseEnglishChecked,
                    onChange = onUseEnglishPress,
                    showCheckmark = showCheckmarksOnSwitches
                )
            }
            if (isTiramisuPlus()) {
                SettingsHorizontalDivider(thickness = 2.dp)
                SettingsPreferenceComponent(
                    label = stringResource(id = CommonR.string.language),
                    value = displayLanguage,
                    doOnPreferenceClick = onSetupLanguagePress,
                )
            }
        }

        // Other
        SettingsGroup(title = {
            Text(text = stringResource(id = CommonR.string.other).uppercase())
        }) {
            if (isPro) {
                SettingsPreferenceComponent(
                    label = stringResource(id = StringsR.string.tip_jar),
                    doOnPreferenceClick = onPurchaseClick,
                    showChevron = true,
                )
                SettingsHorizontalDivider(thickness = 2.dp)
            }
            val flavorName = BuildConfig.FLAVOR
            val storeDisplayName = when (flavorName) {
                "gplay" -> "Google Play"
                "foss" -> "FOSS"
                else -> ""
            }
            val versionName = BuildConfig.VERSION_NAME
            val fullVersionText = "$versionName ($storeDisplayName)"
            SettingsPreferenceComponent(
                label = stringResource(id = CommonR.string.about),
                value = fullVersionText,
                doOnPreferenceClick = onAboutClick,
                showChevron = true,
            )
        }
    }
}

@MyDevices
@Composable
private fun SettingsScreenPreview(
    @PreviewParameter(BooleanPreviewParameterProvider::class) showCheckmarksOnSwitches: Boolean
) {
    AppThemeSurface {
        SettingsScreen(
            goBack = {},
            customizeColors = {},
            customizeWidgetColors = {},
            preventPhoneFromSleeping = false,
            onPreventPhoneFromSleeping = {},
            vibrateOnButtonPressFlow = false,
            onVibrateOnButtonPressFlow = {},
            isPro = false,
            onPurchaseClick = {},
            onAboutClick = {},
            isUseEnglishEnabled = false,
            isUseEnglishChecked = false,
            onUseEnglishPress = {},
            onSetupLanguagePress = {},
            displayLanguage = "English",
            showCheckmarksOnSwitches = showCheckmarksOnSwitches
        )
    }
}
