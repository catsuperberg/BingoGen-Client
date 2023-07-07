package dev.catsuperberg.bingogen.client.model.single.player.game

import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Grid.Companion.toGrid
import dev.catsuperberg.bingogen.client.common.IDurationFormatter
import dev.catsuperberg.bingogen.client.common.MinuteAndSecondDurationFormatter
import dev.catsuperberg.bingogen.client.model.common.BaseModel
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel
import dev.catsuperberg.bingogen.client.service.ITaskBoard
import dev.catsuperberg.bingogen.client.service.ITaskBoardFactory
import dev.catsuperberg.bingogen.client.service.ITaskRetriever
import dev.catsuperberg.bingogen.client.service.TaskApiException
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameModelReceiver
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.BoardTile
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.TaskDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameModel(
    selection: IGameModel.Selection,
    private val receiver: IGameModelReceiver,
    private val retriever: ITaskRetriever,
    private val boardFactory: ITaskBoardFactory,
    modelScope: CoroutineScope = defaultScope,
    private val timeLeftFormatter: IDurationFormatter = MinuteAndSecondDurationFormatter,
) : IGameModel, BaseModel(modelScope) {
    private var board: ITaskBoard? = null
    private var detailsUpdateJob: Job? = null
    private val detailsFlow: MutableStateFlow<TaskDetails?> = MutableStateFlow(null)

    init {
        scope.launch {
            receiver.attachDetailsFlow(detailsFlow)
            requestBoard(selection.game, selection.sheet, selection.sideCount)?.apply {
                board = this
                board?.apply {
                    val anyBingoFlow =
                        combine(this.hasBingo, this.hasKeptBingo) { bingo, keptBingo ->
                            bingo || keptBingo
                        }.stateIn(scope)
                    receiver.attachBingoFlow(anyBingoFlow)
                    receiver.attachBoardFlow(mapTaskBoardToTiles(this))
                }
            }
        }
    }

    private suspend fun requestBoard(game: String, sheet: String, sideCount: Int) : ITaskBoard? {
        return try {
            val grid = retriever.getBoard(sideCount, game, sheet)
            boardFactory.create(grid)
        } catch (e: TaskApiException) {
            receiver.didModelFail("${e.message}")
            null
        }
    }

    private suspend fun mapTaskBoardToTiles(board: ITaskBoard): StateFlow<Grid<BoardTile>> {
        return board.tasks.map { grid ->
            grid.map { task -> BoardTile(task.shortText, task.state.status) }.toGrid()
        }.stateIn(scope)
    }

    override fun requestDetailsUpdates(tileIndex: Int) {
        board?.also { board ->
            detailsUpdateJob = scope.launch {
                board.tasks.map { tasks -> tasks[tileIndex] }.collect { task ->
                    detailsFlow.emit(
                        TaskDetails(
                            task.description,
                            task.state.timeToKeep?.let(timeLeftFormatter::print),
                            task.state.keptFromStart
                        )
                    )
                }
            }
        }
    }

    override fun stopDetailsUpdates() {
        detailsUpdateJob?.cancel()
        scope.launch { detailsFlow.emit(null) }
    }

    override fun toggleTaskDone(tileIndex: Int, state: Boolean?) {
        board?.toggleDone(tileIndex, state)
    }

    override fun toggleTaskTimer(tileIndex: Int, state: Boolean?) {
        board?.toggleTaskTimer(tileIndex, state)
    }

    override fun toggleTaskKeptFromStart(taskIndex: Int, state: Boolean?) {
        board?.toggleKeptFromStart(taskIndex, state)
    }

    override fun restartTaskTimer(tileIndex: Int) {
        board?.resetTaskTimer(tileIndex)
    }

    override fun close() {
        board?.cancelScopeJobs()
        super.close()
    }
}
