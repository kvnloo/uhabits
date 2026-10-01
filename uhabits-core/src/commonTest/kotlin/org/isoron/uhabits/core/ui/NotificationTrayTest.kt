/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 */
package org.isoron.uhabits.core.ui

import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.NumericalHabitType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationTrayTest : BaseUnitTest() {
    @Test
    fun atMostReminderUsesEnteredValueAgainstTarget() {
        val habit = fixtures.createNumericalHabit()
        val today = getToday()
        habit.targetType = NumericalHabitType.AT_MOST
        habit.targetValue = 5.0

        habit.originalEntries.clear()
        habit.recompute()
        assertFalse(isReminderSatisfied(habit, today))

        habit.originalEntries.add(Entry(today, 1000))
        habit.recompute()
        assertTrue(isReminderSatisfied(habit, today))

        habit.originalEntries.add(Entry(today, 5000))
        habit.recompute()
        assertTrue(isReminderSatisfied(habit, today))

        habit.originalEntries.add(Entry(today, 6000))
        habit.recompute()
        assertFalse(isReminderSatisfied(habit, today))
    }

    @Test
    fun atLeastReminderKeepsExistingCompletionSemantics() {
        val habit = fixtures.createNumericalHabit()
        val today = getToday()
        habit.targetType = NumericalHabitType.AT_LEAST
        habit.targetValue = 2.0

        habit.originalEntries.add(Entry(today, 1000))
        habit.recompute()
        assertFalse(isReminderSatisfied(habit, today))

        habit.originalEntries.add(Entry(today, 2000))
        habit.recompute()
        assertTrue(isReminderSatisfied(habit, today))
    }
}
