package dev.catsuperberg.bingogen.client.view.model.common.game

import app.cash.turbine.test
import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.BackHandlerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.joda.time.Duration
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class GameStateTest {
    private val scope = CoroutineScope(Job() + Dispatchers.Unconfined)
    private lateinit var gameState: GameState

    @Before
    fun setup() {
        gameState = GameState()
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun testDidBoardInfoChange() = runTest {
        val testSelection = IGameModel.Selection("test game", "test sheet", 5)
        gameState.boardInfo.test {
            assertNotEquals(testSelection, awaitItem())
            gameState.didBoardInfoChange(testSelection)
            assertEquals(testSelection, awaitItem())
        }
    }

    @Test
    fun testDidTimeChange() = runTest {
        val testDuration = Duration.standardSeconds(10)
        val expectedString = "00:10"
        gameState.time.test {
            assertEquals("", awaitItem())
            gameState.didTimeChange(testDuration)
            assertEquals(expectedString, awaitItem())
        }
    }

    @Test
    fun testDidDetailsChange() = runTest {
        val testDetails = IGameViewModel.TaskDetails.Empty
        gameState.details.test {
            assertNull(awaitItem())
            gameState.didDetailsChange(testDetails)
            assertEquals(testDetails, awaitItem())
        }
    }

    @Test
    fun testBackHandlerStateOnDetailsChange() {
        val expectedOnNull = BackHandlerState.TO_SURE_PROMPT
        val expectedOnDetails = BackHandlerState.TO_GAME_SCREEN
        gameState.didDetailsChange(IGameViewModel.TaskDetails.Empty)
        assertEquals(expectedOnDetails, gameState.backHandlerState.value)
        gameState.didDetailsChange(null)
        assertEquals(expectedOnNull, gameState.backHandlerState.value)
    }

    @Test
    fun testDidStateChange() = runTest {
        val testState = IGameModel.State.ACTIVE
        gameState.state.test {
            assertEquals(IGameModel.State.PREGAME, awaitItem())
            gameState.didStateChange(testState)
            assertEquals(testState, awaitItem())
        }
    }

    @Test
    fun testDidGridChange() = runTest {
        val testGrid = Grid(List(25) { IGameViewModel.BoardTile.Empty.copy(title = "test title") })
        gameState.board.test {
            assertNull(awaitItem())
            gameState.didGridChange(testGrid)
            assertEquals(testGrid, awaitItem())
        }
    }

    @Test
    fun testModelFailedEmitsSnackBarMessage() = runBlocking {
        val testMessages = listOf("Message 1", "Message 2", "Message 3", "Message 4")
        gameState.snackBarMessage.test {
            testMessages.forEach { message ->
                gameState.didModelFail(message)
                val result = awaitItem()
                assertEquals(message, result)
            }
        }
    }

    @Test
    fun testInvokePromptAndExitAbility() {
        val stateToAchieve = BackHandlerState.TO_EXIT_GAME
        assertNotEquals(stateToAchieve, gameState.backHandlerState.value)
        gameState.invokeSurePromptAndExitAbility()
        assertEquals(stateToAchieve, gameState.backHandlerState.value)
    }
}
