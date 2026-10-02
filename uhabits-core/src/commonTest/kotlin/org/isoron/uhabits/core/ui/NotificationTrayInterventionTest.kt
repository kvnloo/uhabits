package org.isoron.uhabits.core.ui

import dev.mokkery.mock
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.Reminder
import org.isoron.uhabits.core.models.WeekdayList
import org.isoron.uhabits.core.preferences.Preferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotificationTrayInterventionTest : BaseUnitTest() {
    private lateinit var systemTray: RecordingSystemTray
    private lateinit var intervention: RecordingIntervention
    private lateinit var tray: NotificationTray

    override fun setUp() {
        super.setUp()
        systemTray = RecordingSystemTray()
        intervention = RecordingIntervention()
        tray = NotificationTray(
            taskRunner,
            commandRunner,
            mock<Preferences>(),
            systemTray,
            intervention
        )
    }

    @Test
    fun eligibleReminderCanBeDeferredBeforeNotificationDelivery() {
        val habit = reminderHabit()
        intervention.deferUntil = 999L

        tray.show(habit, LocalDate(2015, 1, 25), 456L)

        assertEquals(0, systemTray.shown)
        assertEquals(listOf(456L to 999L), intervention.deferred)
        assertTrue(intervention.shown.isEmpty())
    }

    @Test
    fun eligibleControlReminderIsRecordedAndShown() {
        val habit = reminderHabit()

        tray.show(habit, LocalDate(2015, 1, 25), 456L)

        assertEquals(1, systemTray.shown)
        assertEquals(listOf(456L), intervention.shown)
        assertTrue(intervention.deferred.isEmpty())
    }

    @Test
    fun ineligibleReminderNeverReachesExperimentIntervention() {
        val habit = reminderHabit()
        habit.isArchived = true
        intervention.deferUntil = 999L

        tray.show(habit, LocalDate(2015, 1, 25), 456L)

        assertEquals(0, systemTray.shown)
        assertEquals(0, intervention.deferCalls)
        assertTrue(intervention.shown.isEmpty())
        assertTrue(intervention.deferred.isEmpty())
    }

    private fun reminderHabit(): Habit {
        return fixtures.createEmptyHabit().apply {
            reminder = Reminder(8, 30, WeekdayList.EVERY_DAY)
        }
    }

    private class RecordingSystemTray : NotificationTray.SystemTray {
        var shown = 0

        override fun removeNotification(notificationId: Int) {}

        override fun showNotification(
            habit: Habit,
            notificationId: Int,
            date: LocalDate,
            reminderTime: Long
        ) {
            shown += 1
        }

        override fun log(msg: String) {}
    }

    private class RecordingIntervention : ReminderIntervention {
        var deferUntil: Long? = null
        var deferCalls = 0
        val shown = mutableListOf<Long>()
        val deferred = mutableListOf<Pair<Long, Long>>()

        override fun deferUntil(habit: Habit, reminderTime: Long): Long? {
            deferCalls += 1
            return deferUntil
        }

        override fun onShown(reminderTime: Long) {
            shown += reminderTime
        }

        override fun onDeferred(reminderTime: Long, deferredUntil: Long) {
            deferred += reminderTime to deferredUntil
        }
    }
}
