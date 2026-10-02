package org.isoron.uhabits.experiments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.isoron.platform.time.DateUtils
import org.isoron.uhabits.HabitsApplication

private const val TAG = "FlowShieldExperiment"
const val ACTION_SET = "org.isoron.uhabits.EXPERIMENT_SET_FOCUS_SHIELD"
const val ACTION_CLEAR = "org.isoron.uhabits.EXPERIMENT_CLEAR_FOCUS_SHIELD"
const val ACTION_REPORT = "org.isoron.uhabits.EXPERIMENT_REPORT_FOCUS_SHIELD"
const val EXTRA_MINUTES = "minutes"

class FlowShieldExperimentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as HabitsApplication
        val preferences = app.component.preferences

        when (intent.action) {
            ACTION_SET -> {
                val minutes = intent.getIntExtra(EXTRA_MINUTES, 0).coerceIn(1, 120)
                val now = DateUtils.applyTimezone(DateUtils.getLocalTime())
                val until = now + minutes * 60_000L
                preferences.experimentalFocusShieldUntil = until
                Log.i(TAG, "focus_shield=set minutes=$minutes until=$until")
            }

            ACTION_CLEAR -> {
                preferences.experimentalFocusShieldUntil = 0L
                Log.i(TAG, "focus_shield=cleared")
            }

            ACTION_REPORT -> {
                Log.i(
                    TAG,
                    "focus_shield=report until=${preferences.experimentalFocusShieldUntil} " +
                        "deferrals=${preferences.experimentalFocusShieldDeferralCount} " +
                        "last_reminder=${preferences.experimentalFocusShieldLastReminderTime} " +
                        "last_deferred_until=${preferences.experimentalFocusShieldLastDeferredUntil}"
                )
            }
        }
    }
}
