package dev.catsuperberg.bingogen.client.api

import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Task
import dev.catsuperberg.bingogen.client.common.TaskState
import dev.catsuperberg.bingogen.client.common.TaskStatus
import org.joda.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Test

class GridMapperTest {
    private val taskMapper = TaskMapper()
    private val mapper = GridMapper(taskMapper)

    @Test
    fun testMapDtoToGrid() {
        val gridDto = GridDTO(listOf(
            listOf(TaskDTO(1, "Task 1", "Description 1", 1000, true), TaskDTO(2, "Task 2", "Description 2", 2000, false)),
            listOf(TaskDTO(3, "Task 3", "Description 3", 3000, true), TaskDTO(4, "Task 4", "Description 4", 4000, false))
        ))
        val expected = Grid.fromRows(listOf(
            listOf(Task(1, "Task 1", "Description 1", TaskState(Duration.millis(1000), false, TaskStatus.ACTIVE)), Task(2, "Task 2", "Description 2", TaskState(Duration.millis(2000), null, TaskStatus.ACTIVE))),
            listOf(Task(3, "Task 3", "Description 3", TaskState(Duration.millis(3000), false, TaskStatus.ACTIVE)), Task(4, "Task 4", "Description 4", TaskState(Duration.millis(4000), null, TaskStatus.ACTIVE)))
        ))
        val actual = mapper.map(gridDto)
        assertEquals(expected, actual)
    }

    @Test
    fun testMapGridToDto() {
        val grid = Grid.fromRows(listOf(
            listOf(Task(1, "Task 1", "Description 1", TaskState(Duration.millis(1000), false, TaskStatus.ACTIVE)), Task(2, "Task 2", "Description 2", TaskState(Duration.millis(2000), null, TaskStatus.ACTIVE))),
            listOf(Task(3, "Task 3", "Description 3", TaskState(Duration.millis(3000), false, TaskStatus.ACTIVE)), Task(4, "Task 4", "Description 4", TaskState(Duration.millis(4000), null, TaskStatus.ACTIVE)))
        ))
        val expected = GridDTO(listOf(
            listOf(TaskDTO(1, "Task 1", "Description 1", 1000, true), TaskDTO(2, "Task 2", "Description 2", 2000, false)),
            listOf(TaskDTO(3, "Task 3", "Description 3", 3000, true), TaskDTO(4, "Task 4", "Description 4", 4000, false))
        ))
        val actual = mapper.map(grid)
        assertEquals(expected, actual)
    }
}
