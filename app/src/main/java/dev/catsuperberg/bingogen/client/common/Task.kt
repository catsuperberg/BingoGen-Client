package dev.catsuperberg.bingogen.client.common

import org.joda.time.Duration

data class Task(
    val dbid: Long,
    val shortText: String,
    val description: String,
    val state: TaskState
)

enum class TaskStatus { ACTIVE, DONE, FAILED }

data class TaskState(
    val timeToKeep: Duration?,
    val keptFromStart: Boolean?,
    val status: TaskStatus,
)
