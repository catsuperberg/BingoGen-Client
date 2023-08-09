package dev.catsuperberg.bingogen.client.service

import dev.catsuperberg.bingogen.client.common.DefaultTaskGrid
import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

class DefaultTaskBoardFactoryTest {
    private lateinit var taskBoardFactory: DefaultTaskBoardFactory

    @Before
    fun setup() {
        taskBoardFactory = DefaultTaskBoardFactory()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testCreateWithoutScope() {
        val tasks = mock<Grid<Task>>()
        val scope = CoroutineScope(Job() + UnconfinedTestDispatcher())

        val taskBoard = taskBoardFactory.create(tasks, scope)

        assertEquals(tasks, taskBoard.tasks.value)
        assertTrue(taskBoard is TaskBoard)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testCreateWithScope() {
        val tasks = DefaultTaskGrid.grid
        val taskToStart = DefaultTaskGrid.taskIdWithTimeToKeep
        val scope = CoroutineScope(Job() + UnconfinedTestDispatcher())

        val taskBoard = taskBoardFactory.create(tasks, scope)

        val initialCoroutineCount = scope.coroutineContext[Job]?.children?.count()
        initialCoroutineCount?.also { initialCount ->
            val expectedCount = initialCount + 1
            taskBoard.toggleTaskTimer(taskToStart, true)
            val coroutineCount = scope.coroutineContext[Job]?.children?.count()
            assertEquals(expectedCount, coroutineCount)
        } ?: fail("Couldn't get coroutine count")
    }

}
