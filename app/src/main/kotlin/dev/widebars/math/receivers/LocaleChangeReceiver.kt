package dev.widebars.math.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.widebars.math.extensions.updateWidgets

class LocaleChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_LOCALE_CHANGED == intent.action) {
            context.updateWidgets()
        }
    }
}
