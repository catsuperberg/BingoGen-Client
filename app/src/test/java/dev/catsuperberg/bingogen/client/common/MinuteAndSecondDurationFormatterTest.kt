package dev.catsuperberg.bingogen.client.common

import org.joda.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Test

class MinuteAndSecondDurationFormatterTest {
    val formatter = MinuteAndSecondDurationFormatter

    @Test
    fun testZeroMinutes() {
        val duration = Duration.standardSeconds(50)
        val expectedString = "00:50"
        assertEquals(expectedString, formatter.print(duration))
    }

    @Test
    fun testUpToHourMinutes() {
        val duration = Duration.standardMinutes(5) + Duration.standardSeconds(50)
        val expectedString = "05:50"
        assertEquals(expectedString, formatter.print(duration))
    }

    @Test
    fun testOverHourMinutes() {
        val duration = Duration.standardMinutes(105) + Duration.standardSeconds(59)
        val expectedString = "105:59"
        assertEquals(expectedString, formatter.print(duration))
    }

    @Test
    fun testSecondsOverlap() {
        val duration = Duration.standardMinutes(105) + Duration.standardSeconds(60)
        val expectedString = "106:00"
        assertEquals(expectedString, formatter.print(duration))
    }
}
