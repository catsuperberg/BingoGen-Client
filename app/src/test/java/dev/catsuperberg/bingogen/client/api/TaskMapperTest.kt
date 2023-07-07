package dev.catsuperberg.bingogen.client.api

import dev.catsuperberg.bingogen.client.common.Task
import dev.catsuperberg.bingogen.client.common.TaskState
import dev.catsuperberg.bingogen.client.common.TaskStatus
import org.joda.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskMapperTest {
    private val mapper = TaskMapper()
    private val testTask = Task(
        1,
        "Short text",
        "Description",
        TaskState(Duration.millis(1000), false, TaskStatus.ACTIVE)
    )

    @Test
    fun testMapDtoToTask() {
        val dto = TaskDTO(1, "Short text", "Description", 1000, true)
        val expected = testTask
        val actual = mapper.map(dto)
        assertEquals(expected, actual)
    }

    @Test
    fun testMapTaskToDto() {
        val expected = TaskDTO(1, "Short text", "Description", 1000, true)
        val actual = mapper.map(testTask)
        assertEquals(expected, actual)
    }
}
