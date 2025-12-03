package dev.widebars.math.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.widebars.commons.compose.extensions.enableEdgeToEdgeSimple
import dev.widebars.commons.compose.extensions.onEventValue
import dev.widebars.commons.compose.theme.AppThemeSurface
import dev.widebars.commons.extensions.isPro
import dev.widebars.commons.helpers.IS_CUSTOMIZING_COLORS
import dev.widebars.commons.helpers.LICENSE_AUTOFITTEXTVIEW
import dev.widebars.commons.helpers.LICENSE_EVALEX
import dev.widebars.commons.helpers.isTiramisuPlus
import dev.widebars.commons.models.FAQItem
import dev.widebars.math.BuildConfig
import dev.widebars.math.compose.SettingsScreen
import dev.widebars.math.extensions.config
import dev.widebars.math.R
import java.util.Locale
import kotlin.system.exitProcess

class SettingsActivity : SimpleActivity() {

    private val preferences by lazy { config }

    private val productIdX1 = BuildConfig.PRODUCT_ID_X1
    private val productIdX2 = BuildConfig.PRODUCT_ID_X2
    private val productIdX3 = BuildConfig.PRODUCT_ID_X3
    private val subscriptionIdX1 = BuildConfig.SUBSCRIPTION_ID_X1
    private val subscriptionIdX2 = BuildConfig.SUBSCRIPTION_ID_X2
    private val subscriptionIdX3 = BuildConfig.SUBSCRIPTION_ID_X3
    private val subscriptionYearIdX1 = BuildConfig.SUBSCRIPTION_YEAR_ID_X1
    private val subscriptionYearIdX2 = BuildConfig.SUBSCRIPTION_YEAR_ID_X2
    private val subscriptionYearIdX3 = BuildConfig.SUBSCRIPTION_YEAR_ID_X3

    private val productIdList = arrayListOf(productIdX1, productIdX2, productIdX3)
    private val productIdListRu = arrayListOf(productIdX1, productIdX2, productIdX3)
    private val subscriptionIdList = arrayListOf(subscriptionIdX1, subscriptionIdX2, subscriptionIdX3)
    private val subscriptionIdListRu = arrayListOf(subscriptionIdX1, subscriptionIdX2, subscriptionIdX3)
    private val subscriptionYearIdList = arrayListOf(subscriptionYearIdX1, subscriptionYearIdX2, subscriptionYearIdX3)
    private val subscriptionYearIdListRu = arrayListOf(subscriptionYearIdX1, subscriptionYearIdX2, subscriptionYearIdX3)

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSimple()
        setContent {
            AppThemeSurface {
                val context = LocalContext.current
                val preventPhoneFromSleeping by preferences.preventPhoneFromSleepingFlow
                    .collectAsStateWithLifecycle(preferences.preventPhoneFromSleeping)
                val vibrateOnButtonPressFlow by preferences.vibrateOnButtonPressFlow
                    .collectAsStateWithLifecycle(preferences.vibrateOnButtonPress)
                val wasUseEnglishToggledFlow by preferences.wasUseEnglishToggledFlow
                    .collectAsStateWithLifecycle(preferences.wasUseEnglishToggled)
                val useEnglishFlow by preferences.useEnglishFlow
                    .collectAsStateWithLifecycle(preferences.useEnglish)
                val showCheckmarksOnSwitches by config.showCheckmarksOnSwitchesFlow
                    .collectAsStateWithLifecycle(initialValue = config.showCheckmarksOnSwitches)
                val isUseEnglishEnabled by remember(wasUseEnglishToggledFlow) {
                    derivedStateOf {
                        (wasUseEnglishToggledFlow || Locale.getDefault().language != "en") && !isTiramisuPlus()
                    }
                }
                val isPro = onEventValue {
                    context.isPro()
                }
                val displayLanguage = remember { Locale.getDefault().displayLanguage }
                SettingsScreen(
                    displayLanguage = displayLanguage,
                    goBack = ::finish,
                    customizeColors = ::startCustomizationActivity,
                    customizeWidgetColors = ::setupCustomizeWidgetColors,
                    preventPhoneFromSleeping = preventPhoneFromSleeping,
                    onPreventPhoneFromSleeping = preferences::preventPhoneFromSleeping::set,
                    vibrateOnButtonPressFlow = vibrateOnButtonPressFlow,
                    onVibrateOnButtonPressFlow = preferences::vibrateOnButtonPress::set,
                    isPro = isPro,
                    onPurchaseClick = ::startPurchase,
                    onAboutClick = ::launchAbout,
                    isUseEnglishEnabled = isUseEnglishEnabled,
                    isUseEnglishChecked = useEnglishFlow,
                    onUseEnglishPress = { isChecked ->
                        preferences.useEnglish = isChecked
                        exitProcess(0)
                    },
                    onSetupLanguagePress = ::launchChangeAppLanguageIntent,
                    showCheckmarksOnSwitches = showCheckmarksOnSwitches,
                )
            }
        }
    }

    private fun startCustomizationActivity() {
        startCustomizationActivity(
            showAccentColor = false,
            isCollection = false,
            productIdList = productIdList,
            productIdListRu = productIdListRu,
            subscriptionIdList = subscriptionIdList,
            subscriptionIdListRu = subscriptionIdListRu,
            subscriptionYearIdList = subscriptionYearIdList,
            subscriptionYearIdListRu = subscriptionYearIdListRu,
            showAppIconColor = true
        )
    }

    private fun setupCustomizeWidgetColors() {
        Intent(this, WidgetConfigureActivity::class.java).apply {
            putExtra(IS_CUSTOMIZING_COLORS, true)
            startActivity(this)
        }
    }

    private fun startPurchase() {
        startPurchaseActivity(
            R.string.app_launcher_name,
            productIdList = productIdList,
            productIdListRu = productIdListRu,
            subscriptionIdList = subscriptionIdList,
            subscriptionIdListRu = subscriptionIdListRu,
            subscriptionYearIdList = subscriptionYearIdList,
            subscriptionYearIdListRu = subscriptionYearIdListRu,
        )
    }

    private fun launchAbout() {
        val licenses = LICENSE_AUTOFITTEXTVIEW or LICENSE_EVALEX

        val faqItems = arrayListOf(
            FAQItem(R.string.faq_1_title, R.string.faq_1_text),
            FAQItem(
                title = dev.widebars.commons.R.string.faq_1_title_commons,
                text = dev.widebars.commons.R.string.faq_1_text_commons
            ),
            FAQItem(
                title = dev.widebars.commons.R.string.faq_4_title_commons,
                text = dev.widebars.commons.R.string.faq_4_text_commons
            )
        )

        if (!resources.getBoolean(dev.widebars.commons.R.bool.hide_google_relations)) {
            faqItems.add(
                FAQItem(
                    title = dev.widebars.commons.R.string.faq_2_title_commons,
                    text = dev.widebars.commons.R.string.faq_2_text_commons
                )
            )
            faqItems.add(
                FAQItem(
                    title = dev.widebars.commons.R.string.faq_6_title_commons,
                    text = dev.widebars.commons.R.string.faq_6_text_commons
                )
            )
        }

        val flavorName = BuildConfig.FLAVOR
        val storeDisplayName = when (flavorName) {
            "gplay" -> "Google Play"
            "foss" -> "FOSS"
            else -> ""
        }
        val versionName = BuildConfig.VERSION_NAME
        val fullVersionText = "$versionName ($storeDisplayName)"

        startAboutActivity(
            appNameId = R.string.app_name,
            licenseMask = licenses,
            versionName = fullVersionText,
            flavorName = flavorName,
            faqItems = faqItems,
            showFAQBeforeMail = true,
            productIdList = productIdList,
            productIdListRu = productIdListRu,
            subscriptionIdList = subscriptionIdList,
            subscriptionIdListRu = subscriptionIdListRu,
            subscriptionYearIdList = subscriptionYearIdList,
            subscriptionYearIdListRu = subscriptionYearIdListRu,
        )
    }
}
