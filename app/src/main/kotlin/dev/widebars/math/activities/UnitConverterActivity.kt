package dev.widebars.math.activities

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import dev.widebars.commons.extensions.beVisibleIf
import dev.widebars.commons.extensions.getProperPrimaryColor
import dev.widebars.commons.extensions.getProperTextColor
import dev.widebars.commons.extensions.getSurfaceColor
import dev.widebars.commons.extensions.performHapticFeedback
import dev.widebars.commons.extensions.viewBinding
import dev.widebars.commons.helpers.LOWER_ALPHA_INT
import dev.widebars.commons.helpers.MEDIUM_ALPHA_INT
import dev.widebars.commons.helpers.NavigationIcon
import dev.widebars.math.R
import dev.widebars.math.databinding.ActivityUnitConverterBinding
import dev.widebars.math.extensions.config
import dev.widebars.math.extensions.updateViewColors
import dev.widebars.math.helpers.CONVERTER_STATE
import dev.widebars.math.helpers.converters.Converter
import dev.widebars.math.helpers.converters.TemperatureConverter
import dev.widebars.math.helpers.getDecimalSeparator
import dev.widebars.math.views.ConverterView

class UnitConverterActivity : SimpleActivity(), ConverterView.OnUnitChangedListener {
    companion object {
        const val EXTRA_CONVERTER_ID = "converter_id"
    }

    private val binding by viewBinding(ActivityUnitConverterBinding::inflate)
    private var vibrateOnButtonPress = true
    private lateinit var converter: Converter

    private val pillDrawable by lazy {
        ResourcesCompat.getDrawable(
            resources, R.drawable.pill_background, theme
        )?.mutate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.unitConverterToolbar.inflateMenu(R.menu.converter_menu)
            setupOptionsMenu()
        }

        setupEdgeToEdge(padBottomSystem = listOf(binding.nestedScrollview))
        setupMaterialScrollListener(binding.nestedScrollview, binding.unitConverterAppbar)

        val converter = Converter.ALL.getOrNull(intent.getIntExtra(EXTRA_CONVERTER_ID, 0))

        if (converter == null) {
            finish()
            return
        }
        this.converter = converter

        binding.viewUnitConverter.btnClear.setVibratingOnClickListener {
            binding.viewUnitConverter.viewConverter.root.deleteCharacter()
        }
        binding.viewUnitConverter.btnClear.setOnLongClickListener {
            binding.viewUnitConverter.viewConverter.root.clear()
            true
        }

        binding.viewUnitConverter.run {
            arrayOf(
                btnDecimal, btnPlusMinus, btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9
            ).forEach {
                it.setVibratingOnClickListener { view ->
                    binding.viewUnitConverter.viewConverter.root.numpadClicked(view.id)
                }
            }
        }

        binding.viewUnitConverter.viewConverter.root.setOnUnitChangedListener(this)
        binding.viewUnitConverter.viewConverter.root.setConverter(converter)
        binding.unitConverterToolbar.setTitle(converter.nameResId)

        if (savedInstanceState != null) {
            savedInstanceState.getBundle(CONVERTER_STATE)?.also {
                binding.viewUnitConverter.viewConverter.root.restoreFromSavedState(it)
            }
        } else {
            val storedState = config.getLastConverterUnits(converter)
            if (storedState != null) {
                binding.viewUnitConverter.viewConverter.root.updateUnits(
                    newTopUnit = storedState.topUnit,
                    newBottomUnit = storedState.bottomUnit
                )
            }
        }
    }

    private fun setupOptionsMenu() {
        binding.unitConverterToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.swap_units -> binding.viewUnitConverter.viewConverter.root.switch()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    override fun onResume() {
        super.onResume()

        setupTopAppBar(binding.unitConverterAppbar, NavigationIcon.Arrow)
        binding.viewUnitConverter.viewConverter.root.updateColors()
        binding.viewUnitConverter.converterHolder.let {
            updateViewColors(it, getProperTextColor())
        }

        if (config.preventPhoneFromSleeping) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        setupDecimalButton()

        vibrateOnButtonPress = config.vibrateOnButtonPress

        binding.viewUnitConverter.apply {
            val primaryColor = getProperPrimaryColor()

            arrayOf(btnClear).forEach {
                it.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.pill_background,
                    theme
                )?.mutate()
                it.background?.setTint(primaryColor)
//                it.background?.alpha = MEDIUM_ALPHA_INT
            }

            val surfaceColor = getSurfaceColor()
            arrayOf(btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btnDecimal).forEach {
                it.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.pill_background,
                    theme
                )?.mutate()
                it.background?.setTint(surfaceColor)
//                it.background?.alpha = MEDIUM_ALPHA_INT //LOWER_ALPHA_INT
            }

            if (btnPlusMinus.isVisible) {
                updatePlusMinusButton()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (config.preventPhoneFromSleeping) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(
            CONVERTER_STATE,
            binding.viewUnitConverter.viewConverter.root.saveState()
        )
    }

    override fun onUnitsChanged(topUnit: Converter.Unit?, bottomUnit: Converter.Unit?) {
        val isTemperatureConverter = converter.key == TemperatureConverter.key
        val shouldShowNegativeButton =
            isTemperatureConverter && when (topUnit?.key) {
                TemperatureConverter.Unit.Kelvin.key,
                TemperatureConverter.Unit.Rankine.key -> false

                else -> true
            }

        binding.viewUnitConverter.btnPlusMinus.beVisibleIf(shouldShowNegativeButton)
        if (shouldShowNegativeButton) updatePlusMinusButton()
    }

    private fun checkHaptic(view: View) {
        if (vibrateOnButtonPress) {
            view.performHapticFeedback()
        }
    }

    private fun View.setVibratingOnClickListener(callback: (view: View) -> Unit) {
        setOnClickListener {
            callback(it)
            checkHaptic(it)
        }
    }

    private fun setupDecimalButton() {
        binding.viewUnitConverter.btnDecimal.text = getDecimalSeparator()
    }

    private fun updatePlusMinusButton() {
        with(binding.viewUnitConverter) {
            btnPlusMinus.background = pillDrawable
            val primaryColor = getProperPrimaryColor()
            btnPlusMinus.background.setTint(primaryColor)
            btnPlusMinus.background?.alpha = MEDIUM_ALPHA_INT
        }
    }
}
