package dev.widebars.math.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import me.grantland.widget.AutofitHelper
import dev.widebars.commons.extensions.appLaunched
import dev.widebars.commons.extensions.copyToClipboard
import dev.widebars.commons.extensions.getProperPrimaryColor
import dev.widebars.commons.extensions.getProperTextColor
import dev.widebars.commons.extensions.getSurfaceColor
import dev.widebars.commons.extensions.hideKeyboard
import dev.widebars.commons.extensions.performHapticFeedback
import dev.widebars.commons.extensions.toast
import dev.widebars.commons.extensions.value
import dev.widebars.commons.extensions.viewBinding
import dev.widebars.commons.helpers.APP_ICON_IDS
import dev.widebars.commons.helpers.LICENSE_AUTOFITTEXTVIEW
import dev.widebars.commons.helpers.LICENSE_EVALEX
import dev.widebars.commons.helpers.LOWER_ALPHA_INT
import dev.widebars.commons.helpers.MEDIUM_ALPHA_INT
import dev.widebars.commons.models.FAQItem
import dev.widebars.math.BuildConfig
import dev.widebars.math.R
import dev.widebars.math.databases.CalculatorDatabase
import dev.widebars.math.databinding.ActivityMainBinding
import dev.widebars.math.dialogs.HistoryDialog
import dev.widebars.math.extensions.config
import dev.widebars.math.extensions.updateViewColors
import dev.widebars.math.helpers.CALCULATOR_STATE
import dev.widebars.math.helpers.Calculator
import dev.widebars.math.helpers.CalculatorImpl
import dev.widebars.math.helpers.DIVIDE
import dev.widebars.math.helpers.HistoryHelper
import dev.widebars.math.helpers.MINUS
import dev.widebars.math.helpers.MULTIPLY
import dev.widebars.math.helpers.PERCENT
import dev.widebars.math.helpers.PLUS
import dev.widebars.math.helpers.POWER
import dev.widebars.math.helpers.ROOT
import dev.widebars.math.helpers.getDecimalSeparator

class MainActivity : SimpleActivity(), Calculator {
    private var storedTextColor = 0
    private var vibrateOnButtonPress = true
    private var saveCalculatorState: String = ""
    private lateinit var calc: CalculatorImpl

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        useOverflowIcon = false
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        appLaunched(BuildConfig.APPLICATION_ID)
        setupOptionsMenu()
        setupEdgeToEdge(padBottomSystem = listOf(binding.mainNestedScrollview))
        setupMaterialScrollListener(binding.mainNestedScrollview, binding.mainAppbar)

        if (savedInstanceState != null) {
            saveCalculatorState = savedInstanceState.getCharSequence(CALCULATOR_STATE) as String
        }

        calc = CalculatorImpl(
            calculator = this,
            context = applicationContext,
            calculatorState = saveCalculatorState
        )
        binding.btnPlus?.setOnClickOperation(PLUS)
        binding.btnMinus?.setOnClickOperation(MINUS)
        binding.btnMultiply?.setOnClickOperation(MULTIPLY)
        binding.btnDivide?.setOnClickOperation(DIVIDE)
        binding.btnPercent?.setOnClickOperation(PERCENT)
        binding.btnPower?.setOnClickOperation(POWER)
        binding.btnRoot?.setOnClickOperation(ROOT)
        binding.btnRoot?.setOnLongClickListener {
            calc.handleOperation(POWER)
            true
        }
        binding.btnMinus?.setOnLongClickListener { calc.turnToNegative() }
        binding.btnClear?.setVibratingOnClickListener { calc.handleClear() }
        binding.btnClear?.setOnLongClickListener {
            calc.handleReset()
            true
        }

        getButtonIds().forEach {
            it?.setVibratingOnClickListener { view ->
                calc.numpadClicked(view.id)
            }
        }

        binding.btnEquals?.setVibratingOnClickListener { calc.handleEquals() }
        binding.formula?.setOnLongClickListener { copyToClipboard(false) }
        binding.result?.setOnLongClickListener { copyToClipboard(true) }
        binding.btnConvert?.setVibratingOnClickListener { view ->
            launchUnitConverter()
        }
        AutofitHelper.create(binding.result)
        AutofitHelper.create(binding.formula)
        storeStateVariables()
        binding.calculatorHolder?.let { updateViewColors(it, getProperTextColor()) }
        setupDecimalButton()
        checkAppOnSDCard()
    }

    override fun onResume() {
        super.onResume()
        setupTopAppBar(binding.mainAppbar)
        setupMaterialScrollListener(binding.mainNestedScrollview, binding.mainAppbar)
        if (storedTextColor != config.textColor) {
            binding.calculatorHolder?.let { updateViewColors(it, getProperTextColor()) }
        }

        if (config.preventPhoneFromSleeping) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        setupDecimalButton()
        vibrateOnButtonPress = config.vibrateOnButtonPress

        binding.apply {
            val primaryColor = getProperPrimaryColor()
            arrayOf(
                btnMultiply, btnPlus, btnMinus, btnEquals, btnDivide
            ).forEach {
                it?.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.pill_background, theme
                )?.mutate()
                it?.background?.setTint(primaryColor)
//                it?.background?.alpha = MEDIUM_ALPHA_INT
            }

            arrayOf(btnClear, btnReset, btnPower, btnRoot, btnPercent
            ).forEach {
                it?.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.pill_background, theme
                )?.mutate()
                it?.background?.setTint(primaryColor)
                it?.background?.alpha = MEDIUM_ALPHA_INT //LOWER_ALPHA_INT
            }

            val surfaceColor = getSurfaceColor()
            arrayOf(btnConvert, btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9,
                btnDecimal
            ).forEach {
                it?.background = ResourcesCompat.getDrawable(
                    resources, R.drawable.pill_background, theme
                )?.mutate()
                it?.background?.setTint(surfaceColor)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        storeStateVariables()
        if (config.preventPhoneFromSleeping) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            CalculatorDatabase.destroyInstance()
        }
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putString(CALCULATOR_STATE, calc.getCalculatorStateJson().toString())
    }

    private fun setupOptionsMenu() {
        binding.mainToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.history -> showHistory()
                R.id.unit_converter -> launchUnitConverter()
                R.id.settings -> launchSettings()
                R.id.about -> launchAbout()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun storeStateVariables() {
        config.apply {
            storedTextColor = textColor
        }
    }

    private fun checkHaptic(view: View) {
        if (vibrateOnButtonPress) {
            view.performHapticFeedback()
        }
    }

    private fun showHistory() {
        HistoryHelper(this).getHistory {
            if (it.isEmpty()) {
                toast(R.string.history_empty)
            } else {
                HistoryDialog(this, it, calc)
            }
        }
    }

    private fun launchUnitConverter() {
        hideKeyboard()
        startActivity(Intent(applicationContext, UnitConverterPickerActivity::class.java))
    }

    private fun launchSettings() {
        hideKeyboard()
        startActivity(
            Intent(applicationContext, SettingsActivity::class.java).apply {
                putIntegerArrayListExtra(APP_ICON_IDS, getAppIconIDs())
            }
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

        val productIdX1 = BuildConfig.PRODUCT_ID_X1
        val productIdX2 = BuildConfig.PRODUCT_ID_X2
        val productIdX3 = BuildConfig.PRODUCT_ID_X3
        val subscriptionIdX1 = BuildConfig.SUBSCRIPTION_ID_X1
        val subscriptionIdX2 = BuildConfig.SUBSCRIPTION_ID_X2
        val subscriptionIdX3 = BuildConfig.SUBSCRIPTION_ID_X3
        val subscriptionYearIdX1 = BuildConfig.SUBSCRIPTION_YEAR_ID_X1
        val subscriptionYearIdX2 = BuildConfig.SUBSCRIPTION_YEAR_ID_X2
        val subscriptionYearIdX3 = BuildConfig.SUBSCRIPTION_YEAR_ID_X3

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
            flavorName = BuildConfig.FLAVOR,
            faqItems = faqItems,
            showFAQBeforeMail = true,
            productIdList = arrayListOf(productIdX1, productIdX2, productIdX3),
            productIdListRu = arrayListOf(productIdX1, productIdX2, productIdX3),
            subscriptionIdList = arrayListOf(subscriptionIdX1, subscriptionIdX2, subscriptionIdX3),
            subscriptionIdListRu = arrayListOf(subscriptionIdX1, subscriptionIdX2, subscriptionIdX3),
            subscriptionYearIdList = arrayListOf(subscriptionYearIdX1, subscriptionYearIdX2, subscriptionYearIdX3),
            subscriptionYearIdListRu = arrayListOf(subscriptionYearIdX1, subscriptionYearIdX2, subscriptionYearIdX3),
        )
    }

    private fun getButtonIds() = binding.run {
        arrayOf(btnDecimal, btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9)
    }

    private fun copyToClipboard(copyResult: Boolean): Boolean {
        var value = binding.formula?.value
        if (copyResult) {
            value = binding.result?.value
        }

        return if (value.isNullOrEmpty()) {
            false
        } else {
            copyToClipboard(value)
            true
        }
    }

    override fun showNewResult(value: String, context: Context) {
        binding.result?.text = value
    }

    override fun showNewFormula(value: String, context: Context) {
        binding.formula?.text = value
    }

    private fun setupDecimalButton() {
        binding.btnDecimal?.text = getDecimalSeparator()
    }

    private fun View.setVibratingOnClickListener(callback: (view: View) -> Unit) {
        setOnClickListener {
            callback(it)
            checkHaptic(it)
        }
    }

    private fun View.setOnClickOperation(operation: String) {
        setVibratingOnClickListener {
            calc.handleOperation(operation)
        }
    }
}
