/*
 * Downstream research-only seam for reminder interventions.
 */
package org.isoron.uhabits.core.ui

import org.isoron.uhabits.core.models.Habit

/**
 * Runs only after Loop has established that a reminder is otherwise eligible
 * to be shown. Implementations may defer the reminder, but must not mark the
 * habit complete or silently discard the reminder.
 */
interface ReminderIntervention {
    /**
     * Return the persisted deferral timestamp when the reminder was deferred,
     * or null to allow normal notification delivery.
     */
    fun deferUntil(habit: Habit, reminderTime: Long): Long?

    /** Record one otherwise-eligible reminder that was actually allowed through. */
    fun onShown(reminderTime: Long)

    /** Record one otherwise-eligible reminder that was deferred. */
    fun onDeferred(reminderTime: Long, deferredUntil: Long)
}

object NoOpReminderIntervention : ReminderIntervention {
    override fun deferUntil(habit: Habit, reminderTime: Long): Long? = null
    override fun onShown(reminderTime: Long) {}
    override fun onDeferred(reminderTime: Long, deferredUntil: Long) {}
}
