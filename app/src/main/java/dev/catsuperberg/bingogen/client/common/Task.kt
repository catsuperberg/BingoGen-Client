package dev.catsuperberg.bingogen.client.common

import org.joda.time.Duration

data class Task(
    val dbid: Long,
    val shortText: String,
    val description: String,
    val timeToKeep: Duration?,
    val fromStart: Boolean,
)
