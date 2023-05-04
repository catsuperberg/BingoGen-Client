package dev.catsuperberg.bingogen.client.api

import dev.catsuperberg.bingogen.client.common.Task
import org.joda.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskMapperTest {
    private val mapper = TaskMapper()

    @Test
    fun testMapDtoToTask() {
        val dto = TaskDTO(1, "Short text", "Description", 1000, true)
        val expected = Task(1, "Short text", "Description", Duration.millis(1000), true)
        val actual = mapper.map(dto)
        assertEquals(expected, actual)
    }

    @Test
    fun testMapTaskToDto() {
        val task = Task(1, "Short text", "Description", Duration.millis(1000), true)
        val expected = TaskDTO(1, "Short text", "Description", 1000, true)
        val actual = mapper.map(task)
        assertEquals(expected, actual)
    }
}
