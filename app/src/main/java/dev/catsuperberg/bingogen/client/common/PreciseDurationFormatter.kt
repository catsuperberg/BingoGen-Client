package dev.catsuperberg.bingogen.client.common

import org.joda.time.Duration

object PreciseDurationFormatter : IDurationFormatter {
    override fun print(duration: Duration): String {
        val minutes = duration.standardMinutes
        val seconds = duration.standardSeconds % 60
        val milliseconds = duration.millis % 1000
        return "%02d:%02d.%03d".format(minutes, seconds, milliseconds)
    }
}
