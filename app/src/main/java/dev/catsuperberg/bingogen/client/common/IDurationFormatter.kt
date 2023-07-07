package dev.catsuperberg.bingogen.client.common

import org.joda.time.Duration


interface IDurationFormatter {
    fun print(duration: Duration): String
}
