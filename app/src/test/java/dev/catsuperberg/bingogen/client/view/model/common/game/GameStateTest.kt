package dev.catsuperberg.bingogen.client.view.model.common.game

import app.cash.turbine.test
import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Grid.Companion.toGrid
import dev.catsuperberg.bingogen.client.common.TaskStatus
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.BoardTile
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.TaskDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class GameStateTest {
    private val scope = CoroutineScope(Job() + Dispatchers.Unconfined)

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun testAttachDetailsFlow() = runTest {
        val testValue = TaskDetails("", null, null)
        val flow = MutableStateFlow<TaskDetails?>(null)
        val state = GameState()
        scope.launch { state.attachDetailsFlow(flow) }
        state.details.test {
            skipItems(1)
            flow.value = testValue
            assertEquals(testValue, awaitItem())
        }
    }

    @Test
    fun testAttachBoardFlow() = runTest {
        val testValue = Grid(List(9) { BoardTile("", TaskStatus.ACTIVE) } )
        val flow = MutableStateFlow(testValue.map { it.copy(state = TaskStatus.FAILED) }.toGrid())
        val state = GameState()
        scope.launch { state.attachBoardFlow(flow) }
        state.board.test {
            skipItems(1)
            flow.value = testValue
            assertEquals(testValue, awaitItem())
        }
    }

    @Test
    fun testAttachBingoFlow() = runTest {
        val testValue = false
        val flow = MutableStateFlow(true)
        val state = GameState()
        scope.launch { state.attachBingoFlow(flow) }
        state.hasBingo.test {
            skipItems(1)
            flow.value = testValue
            assertEquals(testValue, awaitItem())
        }
    }

    @Test
    fun testCollectedFlowUpdatesWithAttach() = runTest {
        val testValue = TaskDetails("", null, null)
        val flow = MutableStateFlow<TaskDetails?>(null)
        val state = GameState()
        val exposedFlow = state.details
        scope.launch {
            exposedFlow.test {
                skipItems(1)
                state.attachDetailsFlow(flow)
                flow.value = testValue
                assertEquals(testValue, awaitItem())
            }
        }
    }

    @Test
    fun testModelFailedEmitsSnackBarMessage() = runBlocking {
        val testMessages = listOf("Message 1", "Message 2", "Message 3", "Message 4")
        val state = GameState()
        state.snackBarMessage.test {
            testMessages.forEach { message ->
                state.didModelFail(message)
                val result = awaitItem()
                assertEquals(message, result)
            }
        }
    }
}
