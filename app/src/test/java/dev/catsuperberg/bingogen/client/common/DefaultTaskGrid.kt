package dev.catsuperberg.bingogen.client.common

import dev.catsuperberg.bingogen.client.common.Grid.Companion.toGrid
import dev.catsuperberg.bingogen.client.service.TaskBoard

object DefaultTaskGrid {
    private val defaultTaskDuration = TaskBoard.gracePeriod.plus(1_000)

    const val taskIdWithTimeToKeep = 6
    const val taskIdWithTimeToKeepAndKept = 5
    const val taskIdWithUnkeptFailed = 1
    const val taskIdWithTimeToKeepWithoutKeepFromStart = 6

    val grid = Grid(
        listOf(
            Task(0, "0", "0", TaskState(defaultTaskDuration, false, TaskStatus.ACTIVE)),
            Task(1, "1", "1", TaskState(defaultTaskDuration, false, TaskStatus.ACTIVE)),
            Task(2, "2", "2", TaskState(defaultTaskDuration, false, TaskStatus.ACTIVE)),
            Task(3, "3", "3", TaskState(defaultTaskDuration, false, TaskStatus.ACTIVE)),
            Task(4, "4", "4", TaskState(defaultTaskDuration, false, TaskStatus.ACTIVE)),
            Task(5, "5", "5", TaskState(defaultTaskDuration, false, TaskStatus.ACTIVE)),
            Task(6, "6", "6", TaskState(defaultTaskDuration, null, TaskStatus.ACTIVE)),
            Task(7, "7", "7", TaskState(null, false, TaskStatus.ACTIVE)),
            Task(8, "8", "8", TaskState(null, null, TaskStatus.ACTIVE)),
        )
    )

    val failedGrid = grid.map { task -> task.copy(state = task.state.copy(status = TaskStatus.FAILED)) }.toGrid()
}
