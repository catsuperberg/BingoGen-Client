package dev.catsuperberg.bingogen.client.view.model.common.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.MinuteAndSecondDurationFormatter
import dev.catsuperberg.bingogen.client.common.PreciseDurationFormatter
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel.Selection
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel.State
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.BackHandlerState
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.BoardTile
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.NavCallbacks
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.TaskDetails
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.joda.time.Duration

class GameViewModel(
    private val navCallbacks: NavCallbacks,
    override val state: IGameState,
    private val model: IGameModel,
) : ViewModel(), IGameViewModel {
    override fun onBack() {
        when(state.backHandlerState.value) {
            BackHandlerState.TO_GAME_SCREEN -> onCloseDetails()
            BackHandlerState.TO_SURE_PROMPT -> invokeSurePromptAndExit()
            BackHandlerState.TO_EXIT_GAME -> navCallbacks.onBack()
        }
    }

    private fun invokeSurePromptAndExit() {
        state.invokeSurePromptAndExitAbility()
        viewModelScope.launch {
            delay(3_000)
            state.setBackHandlerState(BackHandlerState.TO_SURE_PROMPT)
        }
    }

    override fun onStartBoard() {
        model.requestStartBoard()
    }

    override fun onViewDetails(tileIndex: Int) {
        model.requestDetailsUpdates(tileIndex)
    }

    override fun onCloseDetails() {
        model.stopDetailsUpdates()
    }

    override fun onToggleDone(tileIndex: Int) {
        model.toggleTaskDone(tileIndex)
    }

    override fun onStartTaskTimer(tileIndex: Int) {
        model.toggleTaskTimer(tileIndex, true)
    }

    override fun onStopTaskTimer(tileIndex: Int) {
        model.toggleTaskTimer(tileIndex, false)
    }

    override fun onRestartTaskTimer(tileIndex: Int) {
        model.restartTaskTimer(tileIndex)
    }

    override fun onToggleKeptFromStart(tileIndex: Int) {
        model.toggleTaskKeptFromStart(tileIndex)
    }
}

class GameState() : IGameState {
    private val defaultTimeFormatter = MinuteAndSecondDurationFormatter
    private val bingoTimeFormatter = PreciseDurationFormatter
    override val boardInfo = MutableStateFlow(Selection("", "", 0))
    override val state = MutableStateFlow(State.PREGAME)
    override val time = MutableStateFlow("")
    override val details = MutableStateFlow<TaskDetails?>(null)
    override val board = MutableStateFlow<Grid<BoardTile>?>(null)
    override val backHandlerState = MutableStateFlow(BackHandlerState.TO_SURE_PROMPT)
    override val snackBarMessage = MutableSharedFlow<String>(1)

    override fun didBoardInfoChange(selection: Selection) {
        boardInfo.value = selection
    }

    override fun didTimeChange(durationFromStart: Duration) {
        if(state.value == State.BINGO)
            time.value = bingoTimeFormatter.print(durationFromStart)
        else
            time.value = defaultTimeFormatter.print(durationFromStart)
    }

    override fun didDetailsChange(detailsToDisplay: TaskDetails?) {
        details.value = detailsToDisplay
        backHandlerState.value = if (detailsToDisplay != null) BackHandlerState.TO_GAME_SCREEN else BackHandlerState.TO_SURE_PROMPT
    }

    override fun didStateChange(stateToDisplay: State) {
        state.value = stateToDisplay
    }

    override fun didGridChange(grid: Grid<BoardTile>) {
        board.value = grid
    }

    override suspend fun didModelFail(message: String) {
        snackBarMessage.emit(message)
    }

    override fun invokeSurePromptAndExitAbility() {
        backHandlerState.value = BackHandlerState.TO_EXIT_GAME
    }

    override fun setBackHandlerState(state: BackHandlerState) {
        backHandlerState.value = state
    }
}
