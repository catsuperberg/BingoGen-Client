package dev.catsuperberg.bingogen.client.model.single.player.game

import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Grid.Companion.toGrid
import dev.catsuperberg.bingogen.client.common.IDurationFormatter
import dev.catsuperberg.bingogen.client.common.MinuteAndSecondDurationFormatter
import dev.catsuperberg.bingogen.client.common.Task
import dev.catsuperberg.bingogen.client.common.TaskStatus
import dev.catsuperberg.bingogen.client.model.common.BaseModel
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel.State
import dev.catsuperberg.bingogen.client.service.ITaskBoard
import dev.catsuperberg.bingogen.client.service.ITaskBoardFactory
import dev.catsuperberg.bingogen.client.service.ITaskRetriever
import dev.catsuperberg.bingogen.client.service.TaskApiException
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameModelReceiver
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.BoardTile
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.TaskDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.joda.time.Instant
import org.joda.time.Interval
import kotlin.math.pow

class GameModel(
    private val selection: IGameModel.Selection,
    private val receiver: IGameModelReceiver,
    private val retriever: ITaskRetriever,
    private val boardFactory: ITaskBoardFactory,
    modelScope: CoroutineScope = defaultScope,
    private val timeFormatter: IDurationFormatter = MinuteAndSecondDurationFormatter,
) : IGameModel, BaseModel(modelScope) {
    private val state = MutableStateFlow(State.UNINITIALIZED)

    private var startTime: Instant = Instant.EPOCH
    private var initialGrid: Grid<Task>? = null
    private var board: ITaskBoard? = null

    private var detailsUpdater: Job? = null
    private var boardUpdater: Job? = null
    private var timerTick: Job? = null
    private var bingoStateInitiator: Job? = null

    init {
        scope.launch { state.collect(::onStateChange) }
    }

    private fun onStateChange(currentState: State) {
        receiver.didStateChange(currentState)
        when (currentState) {
            State.UNINITIALIZED -> enterUninitialized(selection)
            State.PREGAME -> { }
            State.ACTIVE -> initialGrid?.also(::enterActive)
            State.BINGO -> enterDone()
        }
    }

    private fun enterUninitialized(selection: IGameModel.Selection) {
        initializeModel(selection)
    }

    private fun initializeModel(selection: IGameModel.Selection) {
        val emptyGrid =
            Grid(List(selection.sideCount.toDouble().pow(2).toInt()) { BoardTile.Empty })
        receiver.didBoardInfoChange(selection)
        receiver.didGridChange(emptyGrid)

        stopAndClearActiveCoroutines()
        resetVariables()

        scope.launch {
            initialGrid = requestGrid(selection.game, selection.sheet, selection.sideCount)
            state.value = State.PREGAME
        }
    }

    private fun enterActive(grid: Grid<Task>) {
        startTime = Instant.now()
        val newBoard = boardFactory.create(grid)
        bingoStateInitiator = startBingoStateInitiator(newBoard)
        timerTick = startTimerTick()
        attachBoardToReceiver(newBoard)
        board = newBoard
    }

    private fun enterDone() {
        manualBoardUpdate()
        stopAndClearActiveCoroutines()
        invokeTimerChange()
        stopAndPreventBoardTimerChanges()
    }

    private fun manualBoardUpdate() {
        board?.tasks?.value?.map { task -> BoardTile(task.shortText, task.state.status.convertBingoState()) }
            ?.toGrid()
            ?.also { tileGrid -> receiver.didGridChange(tileGrid) }
    }


    override fun requestStartBoard() {
        if (initialGrid != null)
            state.value = State.ACTIVE
    }

    override fun requestDetailsUpdates(tileIndex: Int) {
        board?.also { taskBoard ->
            detailsUpdater?.cancel()
            detailsUpdater = scope.launch { collectTaskDetailChanges(taskBoard, tileIndex) }
        }
    }

    private suspend fun collectTaskDetailChanges(board: ITaskBoard, tileIndex: Int) {
        board.tasks.map { tasks -> tasks[tileIndex] }.collect { task ->
            receiver.didDetailsChange(
                TaskDetails(
                    tileIndex,
                    task.description,
                    task.state.timeToKeep?.let(timeFormatter::print),
                    task.state.keptFromStart,
                    if (state.value != State.BINGO) task.state.status else task.state.status.convertBingoState()
                )
            )
        }
    }

    private fun TaskStatus.convertBingoState() = when (this) {
        TaskStatus.COUNTDOWN, TaskStatus.KEPT_COUNTDOWN -> TaskStatus.FINISHED_COUNTDOWN
        TaskStatus.KEPT -> TaskStatus.FINISHED_KEPT
        TaskStatus.UNDONE -> TaskStatus.FINISHED_UNDONE
        TaskStatus.UNKEPT -> TaskStatus.FINISHED_UNKEPT
        else -> this
    }

    override fun stopDetailsUpdates() {
        detailsUpdater?.cancel()
        detailsUpdater = null
        receiver.didDetailsChange(null)
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

    private fun stopAndPreventBoardTimerChanges() {
        board?.apply {
            cancelScopeJobs()
            stopInteractions()
        }
    }


    private fun resetVariables() {
        startTime = Instant.EPOCH
        initialGrid = null
        board = null
    }

    private fun startBingoStateInitiator(newBoard: ITaskBoard) = scope.launch {
        anyBingoFlow(newBoard).collectIfActiveState { hasBingo ->
            if (hasBingo) state.value = State.BINGO
        }
    }

    private fun startTimerTick() = scope.launch {
        while (true) {
            invokeTimerChange()
            delay(1_000)
        }
    }

    private fun invokeTimerChange() {
        val passedTime = Interval(startTime, Instant.now()).toDuration()
        receiver.didTimeChange(passedTime)
    }

    private fun stopAndClearActiveCoroutines() {
        stopTimerTick()
        stopBingoStateInitiator()
        stopDetailsUpdates()
        cancelAndClearBoardUpdater()
    }

    private fun stopBingoStateInitiator() {
        bingoStateInitiator?.cancel()
        bingoStateInitiator = null
    }

    private fun stopTimerTick() {
        timerTick?.cancel()
        timerTick = null
    }

    private fun cancelAndClearBoardUpdater() {
        boardUpdater?.cancel()
        boardUpdater = null
    }

    private fun attachBoardToReceiver(taskBoard: ITaskBoard) {
        cancelAndClearBoardUpdater()
        boardUpdater = scope.launch { mapTaskBoardToTiles(taskBoard).collectIfActiveState(receiver::didGridChange) }
    }

    private fun anyBingoFlow(taskBoard: ITaskBoard) = combine(
        taskBoard.hasBingo,
        taskBoard.hasKeptBingo,
    ) { bingo, keptBingo -> bingo || keptBingo }

    private suspend fun <T> Flow<T>.collectIfActiveState(action: suspend (T) -> Unit) {
        collect { value ->
            if (state.value == State.ACTIVE) action(value)
            else receiver.didModelFail("Illegal flow collected outside ${State.ACTIVE.name} state")
        }
    }

    private suspend fun requestGrid(game: String, sheet: String, sideCount: Int) : Grid<Task>? {
        return try {
            retriever.getBoard(sideCount, game, sheet)
        } catch (e: TaskApiException) {
            receiver.didModelFail("${e.message}")
            null
        }
    }

    private fun mapTaskBoardToTiles(board: ITaskBoard): Flow<Grid<BoardTile>> {
        return board.tasks.map { grid ->
            grid.map { task -> BoardTile(task.shortText, task.state.status) }.toGrid()
        }
    }
}
