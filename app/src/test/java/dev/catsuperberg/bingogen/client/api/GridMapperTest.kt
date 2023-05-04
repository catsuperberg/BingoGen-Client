package dev.catsuperberg.bingogen.client.api

import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Task
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
        val expected = Grid(listOf(
            listOf(Task(1, "Task 1", "Description 1", Duration.millis(1000), true), Task(2, "Task 2", "Description 2", Duration.millis(2000), false)),
            listOf(Task(3, "Task 3", "Description 3", Duration.millis(3000), true), Task(4, "Task 4", "Description 4", Duration.millis(4000), false))
        ))
        val actual = mapper.map(gridDto)
        assertEquals(expected, actual)
    }

    @Test
    fun testMapGridToDto() {
        val grid = Grid(listOf(
            listOf(Task(1, "Task 1", "Description 1", Duration.millis(1000), true), Task(2, "Task 2", "Description 2", Duration.millis(2000), false)),
            listOf(Task(3, "Task 3", "Description 3", Duration.millis(3000), true), Task(4, "Task 4", "Description 4", Duration.millis(4000), false))
        ))
        val expected = GridDTO(listOf(
            listOf(TaskDTO(1, "Task 1", "Description 1", 1000, true), TaskDTO(2, "Task 2", "Description 2", 2000, false)),
            listOf(TaskDTO(3, "Task 3", "Description 3", 3000, true), TaskDTO(4, "Task 4", "Description 4", 4000, false))
        ))
        val actual = mapper.map(grid)
        assertEquals(expected, actual)
    }
}
