package dev.catsuperberg.bingogen.client.service

import app.cash.turbine.test
import app.cash.turbine.testIn
import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Task
import dev.catsuperberg.bingogen.client.common.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class TaskBoardTest {
    private val testData = TestGrids
    private lateinit var scope: CoroutineScope

    @Before
    fun setUp() {
        scope = CoroutineScope(Job() + Dispatchers.Default)
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun testToggleKeptBetweenKeptAndUnkept() = runTest {
        val testId = testData.taskIdWithKeptOnly
        val board = TaskBoard(testData.defaultGrid, this)
        val toggledGrid = testData.defaultGrid.mapIndexed { index, task ->
            if (index != testId) task
            else task.copy(state = task.state.copy(keptFromStart = true, status = TaskStatus.KEPT))
        }
        board.tasks.test(timeout = 500.milliseconds) {
            skipItems(1)
            board.toggleKeptFromStart(testId)
            assertEquals(toggledGrid, awaitItem())
            board.toggleKeptFromStart(testId)
            assertEquals(testData.defaultGrid, awaitItem())
        }
        board.cancelScopeJobs()
    }

    @Test
    fun testToggleTimedTaskBetweenCountdownAndUndone() = runTest {
        val testId = testData.taskIdWithTimeToKeep
        val board = TaskBoard(testData.defaultGrid, this)
        val toggledGrid = testData.defaultGrid.mapIndexed { index, task ->
            if (index != testId) task
            else task.copy(state = task.state.copy(status = TaskStatus.COUNTDOWN))
        }
        board.tasks.test(timeout = 500.milliseconds) {
            skipItems(1)
            board.toggleTaskTimer(testId)
            assertEquals(toggledGrid, awaitItem())
            board.toggleTaskTimer(testId)
            assertEquals(testData.defaultGrid, awaitItem())
        }
        board.cancelScopeJobs()
    }

    @Test
    fun testUnkeptFailAfterGracePeriod() = runTest {
        val board = TaskBoard(testData.defaultGrid, this)
        board.tasks.test(timeout = 500.milliseconds) {
            skipItems(1)
            val result = awaitItem()
            assertEquals(testData.gridWithFailedKept, result)
        }
        board.cancelScopeJobs()
    }

    @Test
    fun testHasBingo() {
        testData.bingoGrids.forEach { grid ->
            val currentScope = CoroutineScope(Job() + Dispatchers.Default)
            val board = TaskBoard(grid, currentScope)
            assertTrue(board.hasBingo.value)
        }
    }

    @Test
    fun testHasNoBingo() {
        val board = TaskBoard(testData.defaultGrid, scope)
        assertFalse(board.hasBingo.value)
    }

    @Test
    fun testSetDone() {
        val indexesToSet = listOf(0, 1)
        val indexToUnset = 1

        val board = TaskBoard(testData.defaultGrid, scope)
        indexesToSet.forEach { board.toggleDone(it, true) }
        val setResult = board.tasks.value

        board.toggleDone(indexToUnset, false)
        val unsetResult = board.tasks.value

        assertEquals(testData.gridWithFirstTwoDone, setResult)
        assertEquals(testData.gridWithFirstDone, unsetResult)
    }

    @Test
    fun testSetDoneIgnoresFailed() {
        val board = TaskBoard(testData.gridWithFailedKept, scope)
        testData.failedTasksAt.forEach { board.toggleDone(it, true) }

        assertEquals(testData.gridWithFailedKept, board.tasks.value)
    }

    @Test
    fun testToggleDone() {
        val indexToToggle = 0

        val board = TaskBoard(testData.defaultGrid, scope)
        board.toggleDone(indexToToggle)
        val toggledResult = board.tasks.value
        board.toggleDone(indexToToggle)
        val unToggledResult = board.tasks.value

        assertEquals(testData.gridWithFirstDone, toggledResult)
        assertEquals(testData.defaultGrid, unToggledResult)
    }

    @Test
    fun testToggleDoneIgnoresFailed() {
        val board = TaskBoard(testData.gridWithFailedKept, scope)
        testData.failedTasksAt.forEach { board.toggleDone(it) }

        assertEquals(testData.gridWithFailedKept, board.tasks.value)
    }

    @Test
    fun testSetKeptFromStart() = runTest {
        val indexToSet = 0
        val indexToToggle = 1

        val board = TaskBoard(testData.defaultGrid, scope)

        board.tasks.test(timeout = 500.milliseconds) {
            skipItems(1)
            board.toggleKeptFromStart(indexToSet, true)
            skipItems(2)
            board.toggleKeptFromStart(indexToToggle)
            skipItems(1)
            val setResult = awaitItem()

            board.toggleKeptFromStart(indexToSet, false)
            board.toggleKeptFromStart(indexToToggle)
            skipItems(1)
            val unsetResult = awaitItem()

            assertEquals(testData.gridWithFirstTwoKept, setResult)
            assertEquals(testData.defaultGrid, unsetResult)
        }
        board.cancelScopeJobs()
    }

    @Test
    fun testToggleKeptFromStart() = runTest {
        val indexToSet = 0
        val indexToToggle = 1

        val board = TaskBoard(testData.defaultGrid, scope)

        board.tasks.test(timeout = 500.milliseconds) {
            skipItems(1)
            board.toggleKeptFromStart(indexToSet, true)
            skipItems(1)
            val setResult = awaitItem()
            board.toggleKeptFromStart(indexToToggle)
            skipItems(1)
            val toggleResult = awaitItem()

            assertEquals(testData.gridWithFirstKept, setResult)
            assertEquals(testData.gridWithFirstTwoKept, toggleResult)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testUnsetKeptFromStartFailsTaskAfterGracePeriod() = runTest {
        val indexToSet = 0
        val indexToToggle = 1

        val board = TaskBoard(testData.defaultGrid, this)
        board.toggleKeptFromStart(indexToSet, true)
        board.toggleKeptFromStart(indexToToggle)

        advanceUntilIdle()
        val resultAfterGracePeriod = board.tasks.value

        board.toggleKeptFromStart(indexToSet, false)
        board.toggleKeptFromStart(indexToToggle)
        val unToggledResult = board.tasks.value

        assertEquals(testData.gridWithFirstTwoKeptAndOthersFailed, resultAfterGracePeriod)
        assertEquals(testData.gridWithFailedKept, unToggledResult)
        board.cancelScopeJobs()
    }

    @Test
    fun testToggleKeptFromStartReversibleDuringGracePeriod() = runTest {
        val indexToUnset = 0
        val indexToToggle = 1

        val board = TaskBoard(testData.defaultGrid, scope)

        board.tasks.test(timeout = 500.milliseconds) {
            skipItems(1)
            board.toggleKeptFromStart(indexToUnset, true)
            skipItems(2)
            board.toggleKeptFromStart(indexToToggle, true)
            skipItems(1)
            val resultsFirstToggle = awaitItem()
            board.toggleKeptFromStart(indexToUnset, false)
            skipItems(1)
            board.toggleKeptFromStart(indexToToggle)
            val resultsSecondToggle = awaitItem()
            board.toggleKeptFromStart(indexToUnset, true)
            skipItems(2)
            board.toggleKeptFromStart(indexToToggle)
            skipItems(1)
            val resultsThirdToggle = awaitItem()

            assertEquals(testData.gridWithFirstTwoKept, resultsFirstToggle)
            assertEquals(testData.defaultGrid, resultsSecondToggle)
            assertEquals(testData.gridWithFirstTwoKept, resultsThirdToggle)
        }
    }

    @Test
    fun testToggleKeptIgnoresNull() = runTest {
        val board = TaskBoard(testData.defaultGrid, this)

        val taskTurbine = board.tasks.testIn(this)
        taskTurbine.skipItems(1)
        testData.nullKeptTasksAt.forEach { board.toggleKeptFromStart(it, true) }
        testData.nullKeptTasksAt.forEach { board.toggleKeptFromStart(it) }

        val events = taskTurbine.cancelAndConsumeRemainingEvents()
        assertTrue(events.isEmpty())

        board.cancelScopeJobs()
    }

    @Test
    fun testTimerTurnsOn() = runTest {
        val taskId = testData.taskIdWithTimeToKeep
        assertNotNull(testData.defaultGrid[taskId].state.timeToKeep)
        val initialTimeToKeep = testData.defaultGrid[taskId].state.timeToKeep!!
        val board = TaskBoard(testData.defaultGrid, this)
        board.tasks.test(timeout = 500.milliseconds) {
            skipItems(1)
            board.toggleTaskTimer(taskId, true)
            skipItems(1)  // state change
            val expectedTimeToKeep = initialTimeToKeep.minus(1_000)
            val result = awaitItem()
            assertEquals(expectedTimeToKeep, result[taskId].state.timeToKeep)
        }
        board.cancelScopeJobs()
    }

    @Test
    fun testRepeatedTimerActivationIgnored() = runTest {
        val taskId = testData.taskIdWithTimeToKeep
        val board = TaskBoard(testData.defaultGrid, this)

        board.toggleTaskTimer(taskId, true)
        val initialTimerCoroutineCount = this.coroutineContext[Job]?.children?.count()

        board.toggleTaskTimer(taskId, true)
        board.toggleTaskTimer(taskId, true)
        val resultCoroutineCount = this.coroutineContext[Job]?.children?.count()
        assertEquals(initialTimerCoroutineCount, resultCoroutineCount)

        board.cancelScopeJobs()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testTimerStops() = runTest {
        val taskId = testData.taskIdWithTimeToKeep
        assertNotNull(testData.defaultGrid[taskId].state.timeToKeep)
        val initialTimeToKeep = testData.defaultGrid[taskId].state.timeToKeep!!
        val board = TaskBoard(testData.defaultGrid, this)
        board.tasks.test(timeout = 500.milliseconds) {
            skipItems(1)
            skipItems(1) // Skip grace period
            board.toggleTaskTimer(taskId, true)
            awaitItem()
            board.toggleTaskTimer(taskId, false)
            advanceTimeBy(initialTimeToKeep.millis)
            skipItems(1) // Reset timer on cancellation causes emission
            expectNoEvents()
        }
        board.cancelScopeJobs()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testTimerResetsAfterStop() = runTest {
        val taskId = testData.taskIdWithTimeToKeep
        assertNotNull(testData.defaultGrid[taskId].state.timeToKeep)
        val initialTimeToKeep = testData.defaultGrid[taskId].state.timeToKeep!!
        val board = TaskBoard(testData.defaultGrid, this)
        board.tasks.test(timeout = 500.milliseconds) {
            skipItems(1)
            board.toggleTaskTimer(taskId, true)
            awaitItem()
            board.toggleTaskTimer(taskId, false)
            advanceTimeBy(initialTimeToKeep.millis)
            cancelAndIgnoreRemainingEvents()
            assertEquals(initialTimeToKeep, board.tasks.value[taskId].state.timeToKeep)
        }
        board.cancelScopeJobs()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testTimerStopsAtZero() = runTest {
        val taskId = testData.taskIdWithTimeToKeep
        assertNotNull(testData.defaultGrid[taskId].state.timeToKeep)
        val initialTimeToKeep = testData.defaultGrid[taskId].state.timeToKeep!!
        val board = TaskBoard(testData.defaultGrid, this)
        board.tasks.test(timeout = 500.milliseconds) {
            skipItems(1)
            skipItems(1) // Skip grace period
            val expectedItems = initialTimeToKeep.standardSeconds.toInt()
            board.toggleTaskTimer(taskId, true)
            skipItems(1) // Skip state change
            advanceTimeBy(initialTimeToKeep.millis*2)
            val caughtEvents = cancelAndConsumeRemainingEvents()
            assertEquals(expectedItems, caughtEvents.count())
        }
        board.cancelScopeJobs()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testTimerFinishMarksTaskAsDone() = runTest {
        val taskId = testData.taskIdWithTimeToKeep
        assertNotNull(testData.defaultGrid[taskId].state.timeToKeep)
        val initialTimeToKeep = testData.defaultGrid[taskId].state.timeToKeep!!
        val board = TaskBoard(testData.defaultGrid, this)
        board.tasks.test(timeout = 500.milliseconds) {
            assertEquals(TaskStatus.UNDONE, board.tasks.value[taskId].state.status)
            skipItems(1)
            board.toggleTaskTimer(taskId, true)
            advanceTimeBy(initialTimeToKeep.millis*2)
            cancelAndConsumeRemainingEvents()
            assertEquals(TaskStatus.DONE, board.tasks.value[taskId].state.status)
        }
        board.cancelScopeJobs()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testTimerFinishMarksKeptTaskAsKept() = runTest {
        val taskId = testData.taskIdWithTimeToKeepAndKept
        assertNotNull(testData.defaultGrid[taskId].state.timeToKeep)
        val initialTimeToKeep = testData.defaultGrid[taskId].state.timeToKeep!!
        val board = TaskBoard(testData.defaultGrid, this)
        board.tasks.test(timeout = 500.milliseconds) {
            assertEquals(TaskStatus.UNKEPT, board.tasks.value[taskId].state.status)
            skipItems(1)
            board.toggleKeptFromStart(taskId)
            board.toggleTaskTimer(taskId, true)
            advanceTimeBy(initialTimeToKeep.millis*2)
            cancelAndConsumeRemainingEvents()
            advanceUntilIdle()
            assertEquals(TaskStatus.KEPT, board.tasks.value[taskId].state.status)
        }
        board.cancelScopeJobs()
    }

    @Test
    fun testToggleKeptFromStartStartsTimerIfPresent() = runTest {
        val taskId = testData.taskIdWithTimeToKeepAndKept
        assertNotNull(testData.defaultGrid[taskId].state.timeToKeep)
        val initialTimeToKeep = testData.defaultGrid[taskId].state.timeToKeep!!
        val board = TaskBoard(testData.defaultGrid, this)
        board.tasks.test(timeout = 500.milliseconds) {
            assertEquals(TaskStatus.UNKEPT, board.tasks.value[taskId].state.status)
            skipItems(1)
            board.toggleKeptFromStart(taskId)
            skipItems(2)
            val expectedTimeToKeep = initialTimeToKeep.minus(1_000)
            val result = awaitItem()
            assertEquals(expectedTimeToKeep, result[taskId].state.timeToKeep)
        }
        board.cancelScopeJobs()
    }

    @Test
    fun testToggleTimerSetsKeptFromStartIfPresent() = runTest {
        val taskId = testData.taskIdWithTimeToKeepAndKept
        assertNotNull(testData.defaultGrid[taskId].state.timeToKeep)
        val board = TaskBoard(testData.defaultGrid, this)
        board.tasks.test(timeout = 500.milliseconds) {
            assertEquals(TaskStatus.UNKEPT, board.tasks.value[taskId].state.status)
            skipItems(1)
            board.toggleTaskTimer(taskId, true)
            skipItems(1)
            val result = awaitItem()
            assertEquals(true, result[taskId].state.keptFromStart)
        }
        board.cancelScopeJobs()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testUnsetOnDoneByTimerTaskResetsIt() = runTest {
        val taskId = testData.taskIdWithTimeToKeepWithoutKeepFromStart
        assertNotNull(testData.defaultGrid[taskId].state.timeToKeep)
        val initialTimeToKeep = testData.defaultGrid[taskId].state.timeToKeep!!
        val board = TaskBoard(testData.defaultGrid, this)
        board.tasks.test(timeout = 500.milliseconds) {
            skipItems(1)
            board.toggleTaskTimer(taskId, true)
            testScheduler.advanceTimeBy(initialTimeToKeep.millis*2)
            cancelAndConsumeRemainingEvents()
            board.toggleDone(taskId, false)
            assertEquals(testData.gridWithFailedKept, board.tasks.value)
        }
        board.cancelScopeJobs()
    }

    @Test
    fun testCantStartTimerOnFailedTask() = runTest {
        val failingTaskId = testData.taskIdWithUnkeptFailed
        assertNotNull(testData.defaultGrid[failingTaskId].state.timeToKeep)
        val board = TaskBoard(testData.defaultGrid, this)
        testScheduler.advanceUntilIdle()
        board.toggleTaskTimer(failingTaskId, true)
        board.cancelScopeJobs()
    }

    @Test
    fun testCantToggleKeptFromStartOnFailedTask() = runTest {
        val failingTaskId = testData.taskIdWithUnkeptFailed
        assertNotNull(testData.defaultGrid[failingTaskId].state.timeToKeep)
        val board = TaskBoard(testData.defaultGrid, this)
        testScheduler.advanceUntilIdle()
        board.toggleDone(failingTaskId, true)
        assertEquals(TaskStatus.FAILED, board.tasks.value[failingTaskId].state.status)
        board.toggleDone(failingTaskId)
        assertEquals(TaskStatus.FAILED, board.tasks.value[failingTaskId].state.status)
        board.cancelScopeJobs()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testTaskDoneByTimerResetsTimerOnUndone() = runTest {
        val taskId = testData.taskIdWithTimeToKeep
        assertNotNull(testData.defaultGrid[taskId].state.timeToKeep)
        val initialTimeToKeep = testData.defaultGrid[taskId].state.timeToKeep!!
        val board = TaskBoard(testData.defaultGrid, this)
        board.tasks.test(timeout = 500.milliseconds) {
            assertEquals(TaskStatus.UNDONE, board.tasks.value[taskId].state.status)
            skipItems(1)
            board.toggleTaskTimer(taskId, true)
            advanceTimeBy(initialTimeToKeep.millis*2)
            cancelAndConsumeRemainingEvents()
            assertEquals(TaskStatus.DONE, board.tasks.value[taskId].state.status)
            board.toggleDone(taskId, false)
            assertEquals(testData.gridWithFailedKept, board.tasks.value)
        }
        board.cancelScopeJobs()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testCancelScopeJobsStopsTimers() = runTest {
        val taskId = testData.taskIdWithTimeToKeep
        assertNotNull(testData.defaultGrid[taskId].state.timeToKeep)
        val initialTimeToKeep = testData.defaultGrid[taskId].state.timeToKeep!!
        val board = TaskBoard(testData.defaultGrid, this)
        board.tasks.test(timeout = 500.milliseconds) {
            skipItems(1)
            skipItems(1) // Skip grace period
            board.toggleTaskTimer(taskId, true)
            awaitItem()
            board.cancelScopeJobs()
            advanceTimeBy(initialTimeToKeep.millis)
            skipItems(1) // Reset timer on cancellation causes emission
            expectNoEvents()
            assertEquals(initialTimeToKeep, testData.defaultGrid[taskId].state.timeToKeep)
        }
        board.cancelScopeJobs()
    }

    @Test
    fun testHasKeptBingo() {
        testData.keptBingoGrids.forEach { grid ->
            val board = TaskBoard(grid, scope)
            assertTrue(gridStateWithKept(board.tasks.value), board.hasKeptBingo.value)
            scope.cancel()
        }
    }

    @Test
    fun testHasNoKeptBingo() {
        testData.bingoGrids.forEach { grid ->
            val board = TaskBoard(grid, scope)
            assertFalse(gridStateWithKept(board.tasks.value), board.hasKeptBingo.value)
            scope.cancel()
        }
    }

    private fun gridStateWithKept(grid: Grid<Task>): String {
        return grid.rows.joinToString("\n") { row ->
            row.map { task ->
                if (task.state.timeToKeep == null && task.state.keptFromStart == true) "~"
                else if (task.state.status == TaskStatus.DONE) "1"
                else "0"
            }.toString()
        }
    }

    @Test
    fun testMarkKeptDoneIfResultsInBingo() {
        testData.keptBingoGrids.parallelStream().forEach { grid ->
            runTest {
                val board = TaskBoard(grid, this)
                val keptBingoTurbine = board.hasKeptBingo.testIn(this)
                val bingoTurbine = board.hasBingo.testIn(this)

                assertTrue(keptBingoTurbine.awaitItem())
                board.markKeptDoneIfResultsInBingo()
                assertFalse(keptBingoTurbine.awaitItem())
                assertTrue(bingoTurbine.expectMostRecentItem())

                keptBingoTurbine.cancelAndIgnoreRemainingEvents()
                bingoTurbine.cancelAndIgnoreRemainingEvents()

                board.cancelScopeJobs()
            }
        }
    }
}
