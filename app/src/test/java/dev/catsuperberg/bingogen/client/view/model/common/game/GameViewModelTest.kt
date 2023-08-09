package dev.catsuperberg.bingogen.client.view.model.common.game

import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.BackHandlerState
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.NavCallbacks
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GameViewModelTest {
    private val testIndex = 5

    @Mock private lateinit var mockNavCallbacks: NavCallbacks
    @Mock private lateinit var mockState: IGameState
    @Mock private lateinit var mockModel: IGameModel

    private lateinit var gameViewModel: GameViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(mockNavCallbacks.onBack).thenReturn(mock())
        gameViewModel = GameViewModel(mockNavCallbacks, mockState, mockModel)
    }

    @Test
    fun testOnBack() {
        val stateFlow = MutableStateFlow(BackHandlerState.TO_EXIT_GAME)
        whenever(mockState.backHandlerState).thenReturn(stateFlow)
        gameViewModel.onBack()
        assertOnBackCallbackCalled()

        stateFlow.value = BackHandlerState.TO_SURE_PROMPT
        gameViewModel.onBack()
        verify(mockState).invokeSurePromptAndExitAbility()

        stateFlow.value = BackHandlerState.TO_GAME_SCREEN
        gameViewModel.onBack()
        verify(mockModel).stopDetailsUpdates()
    }

    @Test
    fun onStartBoard() {
        gameViewModel.onStartBoard()
        verify(mockModel).requestStartBoard()
    }

    @Test
    fun testOnViewDetails() {
        gameViewModel.onViewDetails(testIndex)
        verify(mockModel).requestDetailsUpdates(testIndex)
    }

    @Test
    fun testOnCloseDetails() {
        gameViewModel.onCloseDetails()
        verify(mockModel).stopDetailsUpdates()
    }

    @Test
    fun testOnToggleDone() {
        gameViewModel.onToggleDone(testIndex)
        verify(mockModel).toggleTaskDone(testIndex)
    }

    @Test
    fun testOnStartTaskTimer() {
        gameViewModel.onStartTaskTimer(testIndex)
        verify(mockModel).toggleTaskTimer(testIndex, true)
    }

    @Test
    fun testOnStopTaskTimer() {
        gameViewModel.onStopTaskTimer(testIndex)
        verify(mockModel).toggleTaskTimer(testIndex, false)
    }

    @Test
    fun testOnRestartTaskTimer() {
        gameViewModel.onRestartTaskTimer(testIndex)
        verify(mockModel).restartTaskTimer(testIndex)
    }

    @Test
    fun testOnToggleKeptFromStart() {
        gameViewModel.onToggleKeptFromStart(testIndex)
        verify(mockModel).toggleTaskKeptFromStart(testIndex)
    }

    private fun assertOnBackCallbackCalled() {
        verify(mockNavCallbacks.onBack)()
    }
}
