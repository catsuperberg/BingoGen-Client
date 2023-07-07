package dev.catsuperberg.bingogen.client.view.model.common.gamesetup

import app.cash.turbine.test
import dev.catsuperberg.bingogen.client.view.model.common.gamesetup.IGameSetupState.Direction
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GameSetupStateTest {
    private val testGames = listOf("Game 1", "Game 2", "Game 3", "Game 4")
    private val testSheets = listOf("Sheet 1", "Sheet 2", "Sheet 3", "Sheet 4")

    @Test
    fun testDidLoadGames() {
        val state = GameSetupState()
        state.didLoadGames(testGames)
        assertEquals(testGames, state.gameSelection.value)
    }

    @Test
    fun testDidLoadSheets() {
        val state = GameSetupState()
        state.didLoadSheets(testSheets)
        assertEquals(testSheets, state.sheetSelection.value)
    }

    @Test
    fun testDidServerCallFailedEmitsSnackBarMessage() = runBlocking {
        val testMessages = listOf("Message 1", "Message 2", "Message 3", "Message 4")
        val state = GameSetupState()
        state.snackBarMessage.test {
            testMessages.forEach { message ->
                state.didServerCallFailed(message)
                val result = awaitItem()
                assertEquals(message, result)
            }
        }
    }

    @Test
    fun testSetChosenGame() {
        val state = GameSetupState()
        state.didLoadGames(testGames)
        testGames.forEachIndexed { index, _ ->
            state.setChosenGame(index)
            assertEquals(index, state.chosenGame.value)
        }
    }

    @Test
    fun testExceptionOnInvalidSetChosenGame() {
        val state = GameSetupState()
        state.didLoadGames(testGames)
        assertThrows(IllegalArgumentException::class.java) { state.setChosenGame(testGames.lastIndex + 1)}
    }

    @Test
    fun testSetChosenSheet() {
        val state = GameSetupState()
        state.didLoadSheets(testGames)
        testSheets.forEachIndexed { index, _ ->
            state.setChosenSheet(index)
            assertEquals(index, state.chosenSheet.value)
        }
    }

    @Test
    fun testExceptionOnInvalidSetChosenSheet() {
        val state = GameSetupState()
        state.didLoadSheets(testSheets)
        assertThrows(IllegalArgumentException::class.java) { state.setChosenSheet(testSheets.lastIndex + 1)}
    }

    @Test
    fun testIncrementSideCountUp() {
        val state = GameSetupState()
        val initialValue = state.boardSideCount.value
        state.incrementSideCount(Direction.UP)
        state.incrementSideCount(Direction.UP)
        assertEquals(initialValue+2, state.boardSideCount.value)
    }

    @Test
    fun testIncrementSideCountDown() {
        val state = GameSetupState()
        val initialValue = state.boardSideCount.value
        state.incrementSideCount(Direction.UP)
        state.incrementSideCount(Direction.UP)
        state.incrementSideCount(Direction.DOWN)
        assertEquals(initialValue+1, state.boardSideCount.value)
    }

    @Test
    fun testIncrementSideCountDownIgnoredOnMinSize() {
        val minValue = 2
        val state = GameSetupState()
        while(state.boardSideCount.value > minValue)
            state.incrementSideCount(Direction.DOWN)
        state.incrementSideCount(Direction.DOWN)
        assertEquals(minValue, state.boardSideCount.value)
    }
}
