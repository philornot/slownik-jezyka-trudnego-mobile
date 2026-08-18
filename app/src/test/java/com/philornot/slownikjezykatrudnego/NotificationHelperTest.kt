package com.philornot.slownikjezykatrudnego

import com.philornot.slownikjezykatrudnego.notifications.NotificationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying dynamic notification text generation and
 * pluralization rules.
 */
class NotificationHelperTest {

    @Test
    fun testFormatWordCountPlural_correctInflections() {
        assertEquals("1 słówko", NotificationHelper.formatWordCountPlural(1))
        assertEquals("2 słówka", NotificationHelper.formatWordCountPlural(2))
        assertEquals("3 słówka", NotificationHelper.formatWordCountPlural(3))
        assertEquals("4 słówka", NotificationHelper.formatWordCountPlural(4))
        assertEquals("5 słówek", NotificationHelper.formatWordCountPlural(5))
        assertEquals("12 słówek", NotificationHelper.formatWordCountPlural(12))
        assertEquals("14 słówek", NotificationHelper.formatWordCountPlural(14))
        assertEquals("22 słówka", NotificationHelper.formatWordCountPlural(22))
        assertEquals("25 słówek", NotificationHelper.formatWordCountPlural(25))
    }

    @Test
    fun testGenerateReminderText_withStreakZero_neverEmpty() {
        for (i in 0 until 50) {
            val message = NotificationHelper.generateReminderText(
                streak = 0,
                sessionWords = emptyList(),
                inProgressWords = emptyList(),
                reviewDueCount = 0,
                username = null
            )
            assertTrue(message.isNotBlank())
        }
    }

    @Test
    fun testGenerateReminderText_withActiveWords_includesWordInSomeOutputs() {
        val word = "abnegacja"
        var foundWord = false

        for (i in 0 until 200) {
            val message = NotificationHelper.generateReminderText(
                streak = 2,
                sessionWords = listOf(word),
                inProgressWords = emptyList(),
                reviewDueCount = 1,
                username = null
            )
            if (message.contains(word)) {
                foundWord = true
                break
            }
        }

        assertTrue("Expected at least one generated message to feature the active word", foundWord)
    }

    @Test
    fun testGenerateReminderText_withUsername_includesUsernameInSomeOutputs() {
        val username = "Filip"
        var foundUser = false

        for (i in 0 until 200) {
            val message = NotificationHelper.generateReminderText(
                streak = 5,
                sessionWords = emptyList(),
                inProgressWords = emptyList(),
                reviewDueCount = 0,
                username = username
            )
            if (message.contains(username)) {
                foundUser = true
                break
            }
        }

        assertTrue("Expected at least one generated message to feature the username", foundUser)
    }

    @Test
    fun testGenerateReminderText_withoutUsername_neverMentionsNullOrPlaceholder() {
        for (i in 0 until 100) {
            val message = NotificationHelper.generateReminderText(
                streak = 10,
                sessionWords = listOf("imponderabilia"),
                inProgressWords = emptyList(),
                reviewDueCount = 3,
                username = null
            )
            assertFalse(message.contains("null"))
            assertFalse(message.contains("Cześć !"))
        }
    }

    @Test
    fun testGenerateReminderText_withHighStreak_generatesAppropriateContent() {
        var foundHighStreakIndicator = false

        for (i in 0 until 200) {
            val message = NotificationHelper.generateReminderText(
                streak = 15,
                sessionWords = emptyList(),
                inProgressWords = emptyList(),
                reviewDueCount = 0,
                username = null
            )
            if (message.contains("15 dni") || message.contains("Imponująca seria") || message.contains(
                    "Mistrzowska dyscyplina"
                )
            ) {
                foundHighStreakIndicator = true
                break
            }
        }

        assertTrue("Expected high streak message to be picked", foundHighStreakIndicator)
    }
}
