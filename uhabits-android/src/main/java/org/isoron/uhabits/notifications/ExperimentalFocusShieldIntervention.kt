package org.isoron.uhabits.notifications

import me.tatarka.inject.annotations.Inject
import org.isoron.uhabits.BuildConfig
import org.isoron.platform.time.DateUtils
import org.isoron.uhabits.core.AppScope
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.reminders.ReminderScheduler
import org.isoron.uhabits.core.ui.ReminderIntervention

@Inject
@AppScope
class ExperimentalFocusShieldIntervention(
    private val preferences: Preferences,
    private val reminderScheduler: ReminderScheduler
) : ReminderIntervention {
    override fun deferUntil(habit: Habit, reminderTime: Long): Long? {
        // A release build must never carry over an experimental debug lease.
        if (!BuildConfig.DEBUG) return null
        val now = DateUtils.applyTimezone(DateUtils.getLocalTime())
        val shieldUntil = preferences.experimentalFocusShieldUntil
        if (shieldUntil <= now) return null

        reminderScheduler.snoozeUntil(habit, shieldUntil)
        return shieldUntil
    }

    override fun onShown(reminderTime: Long) {
        if (!BuildConfig.DEBUG) return
        preferences.recordExperimentalFocusShieldReminderEvent(
            observedEpochMillis = System.currentTimeMillis(),
            reminderTime = reminderTime,
            deferredUntil = 0L,
            shielded = false
        )
    }

    override fun onDeferred(reminderTime: Long, deferredUntil: Long) {
        if (!BuildConfig.DEBUG) return
        preferences.recordExperimentalFocusShieldReminderEvent(
            observedEpochMillis = System.currentTimeMillis(),
            reminderTime = reminderTime,
            deferredUntil = deferredUntil,
            shielded = true
        )
    }
}
