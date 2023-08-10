package dev.catsuperberg.bingogen.client.common

import org.joda.time.Duration

data class Task(
    val dbid: Long,
    val shortText: String,
    val description: String,
    val state: TaskState
)

enum class TaskStatus {
    UNDONE, UNKEPT, DONE, KEPT, COUNTDOWN, KEPT_COUNTDOWN, FAILED, INACTIVE, FINISHED_UNDONE, FINISHED_UNKEPT, FINISHED_KEPT, FINISHED_COUNTDOWN;

    companion object {
        val WithActiveTimer = listOf(COUNTDOWN, KEPT_COUNTDOWN)
        val Finished = listOf(FINISHED_UNDONE, FINISHED_UNKEPT, FINISHED_KEPT, FINISHED_COUNTDOWN)
    }
}

data class TaskState(
    val timeToKeep: Duration?,
    val keptFromStart: Boolean?,
    val status: TaskStatus,
)
