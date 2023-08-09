package dev.catsuperberg.bingogen.client.common

import org.joda.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Test

class PreciseDurationFormatterTest {
    val formatter = PreciseDurationFormatter

    @Test
    fun testNoMillis() {
        val duration = Duration.standardSeconds(50)
        val expectedString = "00:50.000"
        assertEquals(expectedString, formatter.print(duration))
    }

    @Test
    fun testZeroMinutes() {
        val duration = Duration.standardSeconds(50).plus(5)
        val expectedString = "00:50.005"
        assertEquals(expectedString, formatter.print(duration))
    }

    @Test
    fun testUpToHourMinutes() {
        val duration = Duration.standardMinutes(5) + Duration.standardSeconds(50).plus(5)
        val expectedString = "05:50.005"
        assertEquals(expectedString, formatter.print(duration))
    }

    @Test
    fun testOverHourMinutes() {
        val duration = Duration.standardMinutes(105) + Duration.standardSeconds(59).plus(5)
        val expectedString = "105:59.005"
        assertEquals(expectedString, formatter.print(duration))
    }

    @Test
    fun testSecondsOverlap() {
        val duration = Duration.standardMinutes(105) + Duration.standardSeconds(60).plus(5)
        val expectedString = "106:00.005"
        assertEquals(expectedString, formatter.print(duration))
    }
}
