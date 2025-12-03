package dev.widebars.math.helpers

import android.content.Context
import kotlinx.coroutines.flow.Flow
import dev.widebars.commons.helpers.BaseConfig
import dev.widebars.math.helpers.converters.Converter
import dev.widebars.math.models.ConverterUnitsState

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    fun getLastConverterUnits(converter: Converter): ConverterUnitsState? {
        val storedState = prefs.getString("$CONVERTER_UNITS_PREFIX.${converter.key}", null)
        return if (!storedState.isNullOrEmpty()) {
            val parts = storedState.split(",").map { part ->
                converter.units.first { it.key == part }
            }
            if (parts.size == 2) {
                ConverterUnitsState(parts[0], parts[1])
            } else {
                null
            }
        } else {
            null
        }
    }

    fun putLastConverterUnits(
        converter: Converter,
        topUnit: Converter.Unit,
        bottomUnit: Converter.Unit
    ) {
        prefs.edit().putString(
            "$CONVERTER_UNITS_PREFIX.${converter.key}",
            "${topUnit.key},${bottomUnit.key}"
        ).apply()
    }

    val preventPhoneFromSleepingFlow: Flow<Boolean> = ::preventPhoneFromSleeping.asFlowNonNull()
    val vibrateOnButtonPressFlow: Flow<Boolean> = ::vibrateOnButtonPress.asFlowNonNull()
}
