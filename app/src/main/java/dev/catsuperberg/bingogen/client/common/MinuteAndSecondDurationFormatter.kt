package dev.catsuperberg.bingogen.client.common

import org.joda.time.Duration

object MinuteAndSecondDurationFormatter : IDurationFormatter {
    override fun print(duration: Duration): String {
        val minutes = duration.standardMinutes
        val seconds = duration.standardSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
