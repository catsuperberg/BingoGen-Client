package dev.catsuperberg.bingogen.client.view.model.common.game

import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.TaskStatus
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel.Selection
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel.State
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.BackHandlerState
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.BoardTile
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.TaskDetails
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.joda.time.Duration

interface IGameFields {
    val boardInfo: StateFlow<Selection>
    val state: StateFlow<State>
    val time: StateFlow<String>
    val details: StateFlow<TaskDetails?>
    val board: StateFlow<Grid<BoardTile>?>
    val backHandlerState: StateFlow<BackHandlerState>
    val snackBarMessage: SharedFlow<String>
}

interface IGameRequests {
    fun onBack()
    fun onStartBoard()
    fun onViewDetails(tileIndex: Int)
    fun onCloseDetails()
    fun onToggleDone(tileIndex: Int)
    fun onStartTaskTimer(tileIndex: Int)
    fun onStopTaskTimer(tileIndex: Int)
    fun onRestartTaskTimer(tileIndex: Int)
    fun onToggleKeptFromStart(tileIndex: Int)
}

interface IGameViewModel : IGameRequests {
    val state: IGameFields

    data class NavCallbacks(val onBack: () -> Unit)
    data class BoardTile(val title: String, val state: TaskStatus) {
        companion object {
            val Empty = BoardTile("", TaskStatus.INACTIVE)
        }
    }
    enum class BackHandlerState { TO_GAME_SCREEN, TO_SURE_PROMPT, TO_EXIT_GAME }
    data class TaskDetails(
        val gridId: Int,
        val description: String,
        val timeRemaining: String?,
        val keptFromStart: Boolean?,
        val status: TaskStatus
    ) {
        companion object {
            val Empty = TaskDetails(0, "", null, null, TaskStatus.INACTIVE)
        }
    }
}

interface IGameModelReceiver {
    fun didBoardInfoChange(selection: Selection)
    fun didTimeChange(durationFromStart: Duration)
    fun didDetailsChange(detailsToDisplay: TaskDetails?)
    fun didStateChange(stateToDisplay: State)
    fun didGridChange(grid: Grid<BoardTile>)
    suspend fun didModelFail(message: String)
}

interface IGameState: IGameFields, IGameModelReceiver {
    fun invokeSurePromptAndExitAbility()
    fun setBackHandlerState(state: BackHandlerState)
}
