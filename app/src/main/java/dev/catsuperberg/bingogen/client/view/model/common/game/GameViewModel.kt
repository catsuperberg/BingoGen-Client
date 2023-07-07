package dev.catsuperberg.bingogen.client.view.model.common.game

import androidx.lifecycle.ViewModel
import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.BoardTile
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.NavCallbacks
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.TaskDetails
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameViewModel(
    private val navCallbacks: NavCallbacks,
    override val state: IGameState,
    private val model: IGameModel,
) : ViewModel(), IGameViewModel {
    override fun onBack() {
        navCallbacks.onBack()
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

    override fun onBingo() {
        onBack()
    }
}

class GameState : IGameState {
    override val details = MutableStateFlow<TaskDetails?>(null)
    override val board = MutableStateFlow<Grid<BoardTile>?>(null)
    override val hasBingo = MutableStateFlow(false)
    override val snackBarMessage = MutableSharedFlow<String>()

    override suspend fun attachDetailsFlow(detailsFlow: StateFlow<TaskDetails?>) {
        detailsFlow.collect { details.value = it }
    }

    override suspend fun attachBingoFlow(bingoFlow: StateFlow<Boolean>) {
        bingoFlow.collect { hasBingo.value = it }
    }

    override suspend fun attachBoardFlow(tileFlow: StateFlow<Grid<BoardTile>>) {
        tileFlow.collect { board.value = it }
    }

    override suspend fun didModelFail(message: String) {
        snackBarMessage.emit(message)
    }
}
