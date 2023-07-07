package dev.catsuperberg.bingogen.client.view.model.common.game

import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.TaskStatus
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.BoardTile
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.TaskDetails
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface IGameFields {
    val details: StateFlow<TaskDetails?>
    val board: StateFlow<Grid<BoardTile>?>
    val snackBarMessage: SharedFlow<String>
    val hasBingo: StateFlow<Boolean>
}

interface IGameRequests {
    fun onBack()
    fun onViewDetails(tileIndex: Int)
    fun onCloseDetails()
    fun onToggleDone(tileIndex: Int)
    fun onStartTaskTimer(tileIndex: Int)
    fun onStopTaskTimer(tileIndex: Int)
    fun onRestartTaskTimer(tileIndex: Int)
    fun onToggleKeptFromStart(tileIndex: Int)
    fun onBingo()
}

interface IGameViewModel : IGameRequests {
    val state: IGameFields

    data class NavCallbacks(val onBack: () -> Unit)
    data class BoardTile(val title: String, val state: TaskStatus)
    data class TaskDetails(val description: String, val timeRemaining: String?, val keptFromStart: Boolean?) {
        companion object {
            val Empty = TaskDetails("", null, null)
        }
    }
}

interface IGameModelReceiver {
    suspend fun attachDetailsFlow(detailsFlow: StateFlow<TaskDetails?>)
    suspend fun attachBingoFlow(bingoFlow: StateFlow<Boolean>)
    suspend fun attachBoardFlow(tileFlow: StateFlow<Grid<BoardTile>>)
    suspend fun didModelFail(message: String)
}

interface IGameState: IGameFields, IGameModelReceiver
