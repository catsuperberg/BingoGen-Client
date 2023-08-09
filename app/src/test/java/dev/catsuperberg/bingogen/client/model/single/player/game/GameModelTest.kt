package dev.catsuperberg.bingogen.client.model.single.player.game

import app.cash.turbine.test
import dev.catsuperberg.bingogen.client.common.DefaultTaskGrid
import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Grid.Companion.toGrid
import dev.catsuperberg.bingogen.client.common.MinuteAndSecondDurationFormatter
import dev.catsuperberg.bingogen.client.common.Task
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel.State
import dev.catsuperberg.bingogen.client.service.ITaskBoard
import dev.catsuperberg.bingogen.client.service.ITaskBoardFactory
import dev.catsuperberg.bingogen.client.service.ITaskRetriever
import dev.catsuperberg.bingogen.client.service.TaskApiException
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameModelReceiver
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.TaskDetails
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.joda.time.Duration
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.timeout
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GameModelTest {
    private val testData = object {
        val exceptionMessage = "test"
        val selection = IGameModel.Selection("test", "sheet", 5)
        val grid = DefaultTaskGrid.grid
        val idToRequest = grid.indices.first
        val boardTileGrid = grid.map { task -> IGameViewModel.BoardTile(task.shortText, task.state.status) }
            .toGrid()
    }

    @Mock private lateinit var mockReceiver: IGameModelReceiver
    @Mock private lateinit var mockRetriever: ITaskRetriever
    @Mock private lateinit var mockBoard: ITaskBoard
    @Mock private lateinit var mockBoardFactory: ITaskBoardFactory

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(mockRetriever.getBoard(any(), any(), any())).thenReturn(testData.grid)
        whenever(mockBoardFactory.create(any(), anyOrNull())).thenReturn(mockBoard)
        whenever(mockBoard.tasks).thenReturn(MutableStateFlow(testData.grid))
        whenever(mockBoard.hasBingo).thenReturn(MutableStateFlow(false))
        whenever(mockBoard.hasKeptBingo).thenReturn(MutableStateFlow(false))
    }

    @Test
    fun testSetsBoardInfo() = runTest {
        GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        verify(mockReceiver, timeout(500)).didBoardInfoChange(testData.selection)
    }

    @Test
    fun testComesOutOfUninitializedState() = runTest {
        GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        verify(mockReceiver, timeout(500)).didStateChange(State.UNINITIALIZED)
        verify(mockReceiver, timeout(500)).didStateChange(State.PREGAME)
    }

    @Test
    fun testStartBoard() = runTest {
        val model = GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        verify(mockReceiver, timeout(500)).didStateChange(State.UNINITIALIZED)
        verify(mockReceiver, timeout(500)).didStateChange(State.PREGAME)
        model.requestStartBoard()
        verify(mockReceiver, timeout(500)).didStateChange(State.ACTIVE)
        verify(mockReceiver, timeout(500).times(1)).didGridChange(testData.boardTileGrid)
    }

    @Test
    fun testStartIgnoredWhenActive() = runTest {
        val model = GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        verify(mockReceiver, timeout(500)).didStateChange(State.UNINITIALIZED)
        verify(mockReceiver, timeout(500)).didStateChange(State.PREGAME)
        model.requestStartBoard()
        model.requestStartBoard()

        verify(mockReceiver, timeout(500).times(1)).didStateChange(State.ACTIVE)
        verify(mockReceiver, timeout(500).times(1)).didGridChange(testData.boardTileGrid)
    }

    @Test
    fun testAnyBingoFinishesBoard() = runTest {
        val bingoFlow = MutableStateFlow(false)
        val keptBingoFlow = MutableStateFlow(false)
        whenever(mockBoard.hasBingo).thenReturn(bingoFlow)
        whenever(mockBoard.hasKeptBingo).thenReturn(keptBingoFlow)

        verifyBingoStateOnEmission { bingoFlow.value = true }
        bingoFlow.value = false
        verifyBingoStateOnEmission { keptBingoFlow.value = true }
    }

    private fun verifyBingoStateOnEmission(flowEmission: () -> Unit) {
        val receiver = mock(IGameModelReceiver::class.java)
        val bingoModel = GameModel(testData.selection, receiver, mockRetriever, mockBoardFactory)
        verify(receiver, timeout(500)).didStateChange(State.PREGAME)
        bingoModel.requestStartBoard()
        verify(receiver, timeout(500)).didStateChange(State.ACTIVE)
        flowEmission()
        verify(receiver, timeout(500)).didStateChange(State.BINGO)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testTimeUpdater() = runTest {
        val testScope = TestScope(UnconfinedTestDispatcher())
        val timeCollectorFlow = MutableSharedFlow<Duration>()
        whenever(mockReceiver.didTimeChange(any())).then {
            this.launch { timeCollectorFlow.emit(Duration.ZERO) }
            Unit
        }

        val secondsToCheck = 5
        val model = GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory, modelScope = testScope)
        advanceUntilIdle()
        timeCollectorFlow.test {
            model.requestStartBoard()
            testScope.advanceTimeBy(100)
            repeat(secondsToCheck) { testScope.advanceTimeBy(1_000) }
            cancelAndIgnoreRemainingEvents()
        }
        testScope.cancel()
    }

    @Test
    fun testHandlerCatchesUncaughtException() = runTest {
        val exceptions = MutableStateFlow<List<String>>(listOf())
        val handler = customExceptionHandler(exceptions)
        val scope = CoroutineScope(Job() + handler)

        whenever(mockBoardFactory.create(any(), anyOrNull())).then { throw Exception(testData.exceptionMessage) }

        val model = GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory, scope)

        exceptions.test {
            skipItems(1)
            model.requestStartBoard()
            assertEquals(testData.exceptionMessage, awaitItem().first())
        }
    }

    @Test
    fun testHandlerWithoutException() = runTest {
        val exceptions = MutableStateFlow<List<String>>(listOf())
        val handler = customExceptionHandler(exceptions)
        val scope = CoroutineScope(Job() + handler)

        val model = GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory, scope)
        model.requestStartBoard()

        exceptions.test {
            skipItems(1)
            model.requestStartBoard()
            expectNoEvents()
        }
        verify(mockReceiver, never()).didModelFail(any())
    }

    @Test
    fun testInitAttachesBoardFlow() = runTest {
        GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        verify(mockReceiver, timeout(500)).didGridChange(any())
    }

    @Test
    fun testInitCallsDidLoadBoardFailed() = runTest {
        initializeModelWithFailingBoardRetrieval()
        verify(mockReceiver, timeout(500)).didModelFail(testData.exceptionMessage)
    }

    @Test
    fun testInitAttachesBingoFlow() = runTest {
        GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        verify(mockReceiver, timeout(500)).didStateChange(any())
    }

    @Test
    fun testDetailsRequestIgnoredWhenNoBoard() = runTest {
        val model = initializeModelWithFailingBoardRetrieval()
        model.requestDetailsUpdates(testData.idToRequest)
        verify(mockReceiver, timeout(500).times(1)).didDetailsChange(null)
    }

    @Test
    fun testRequestDetailsUpdates() = runTest {
        val detailsFlow = MutableSharedFlow<TaskDetails?>()
        whenever(mockReceiver.didDetailsChange(anyOrNull())).then { invocation ->
            val details = invocation.arguments[0] as TaskDetails?
            this.launch { detailsFlow.emit(details) }
            Unit
        }

        val boardFlow = MutableStateFlow(testData.grid)
        whenever(mockBoard.tasks).thenReturn(boardFlow)
        val durationFormatter = MinuteAndSecondDurationFormatter
        val model = GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory, timeFormatter =  durationFormatter)
        waitForBoardCreation(model)

        detailsFlow.test {
            skipItems(1)
            model.requestDetailsUpdates(testData.idToRequest)
            skipItems(1)

            repeat(3) {
                val newGrid = multiplyTimeToKeep(boardFlow, testData.idToRequest)
                boardFlow.value = newGrid

                val expectedTime = newGrid[testData.idToRequest].state.timeToKeep?.let(durationFormatter::print)
                val receivedTime = awaitItem()?.timeRemaining
                assertEquals(expectedTime, receivedTime)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testStopDetailsUpdates() = runTest {
        val detailsFlow = MutableSharedFlow<TaskDetails?>()
        whenever(mockReceiver.didDetailsChange(anyOrNull())).then { invocation ->
            val details = invocation.arguments[0] as TaskDetails?
            println(details)
            this.launch { detailsFlow.emit(details) }
            Unit
        }

        val boardFlow = MutableStateFlow(testData.grid)
        whenever(mockBoard.tasks).thenReturn(boardFlow)
        val updateValue = { boardFlow.value = multiplyTimeToKeep(boardFlow, testData.idToRequest) }
        val model = GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        waitForBoardCreation(model)

        detailsFlow.test {
            skipItems(1)
            model.requestDetailsUpdates(testData.idToRequest)
            skipItems(1)

            updateValue()
            skipItems(1)
            model.stopDetailsUpdates()
            assertNull(awaitItem())
            updateValue()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun multiplyTimeToKeep(boardFlow: MutableStateFlow<Grid<Task>>, id: Int): Grid<Task> {
        return boardFlow.value.mapIndexed { index, task ->
            if (index != id) task
            else task.copy(state = task.state.copy(timeToKeep = task.state.timeToKeep?.multipliedBy(2)))
        }.toGrid()
    }

    @Test
    fun testToggleTaskDone() = runTest {
        val model = GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        model.requestStartBoard()
        waitForBoardCreation(model)
        model.toggleTaskDone(testData.idToRequest)
        verify(mockBoard, timeout(500)).toggleDone(testData.idToRequest, null)
        model.toggleTaskDone(testData.idToRequest, true)
        verify(mockBoard, timeout(500)).toggleDone(testData.idToRequest, true)
        model.toggleTaskDone(testData.idToRequest, false)
        verify(mockBoard, timeout(500)).toggleDone(testData.idToRequest, false)
    }

    @Test
    fun testToggleTaskTimer() = runTest {
        val model = GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        model.requestStartBoard()
        waitForBoardCreation(model)
        model.toggleTaskTimer(testData.idToRequest)
        verify(mockBoard, timeout(500)).toggleTaskTimer(testData.idToRequest, null)
        model.toggleTaskTimer(testData.idToRequest, true)
        verify(mockBoard, timeout(500)).toggleTaskTimer(testData.idToRequest, true)
        model.toggleTaskTimer(testData.idToRequest, false)
        verify(mockBoard, timeout(500)).toggleTaskTimer(testData.idToRequest, false)
    }

    @Test
    fun testToggleTaskKeptFromStart() = runTest {
        val model = GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        model.requestStartBoard()
        waitForBoardCreation(model)
        model.toggleTaskKeptFromStart(testData.idToRequest)
        verify(mockBoard, timeout(500)).toggleKeptFromStart(testData.idToRequest, null)
        model.toggleTaskKeptFromStart(testData.idToRequest, true)
        verify(mockBoard, timeout(500)).toggleKeptFromStart(testData.idToRequest, true)
        model.toggleTaskKeptFromStart(testData.idToRequest, false)
        verify(mockBoard, timeout(500)).toggleKeptFromStart(testData.idToRequest, false)
    }

    @Test
    fun testRestartTaskTimer() = runTest {
        val model = GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        waitForBoardCreation(model)
        model.restartTaskTimer(testData.idToRequest)
        verify(mockBoard, timeout(500)).resetTaskTimer(testData.idToRequest)
    }


    private fun initializeModelWithFailingBoardRetrieval(): GameModel {
        whenever(mockRetriever.getBoard(any(), any(), any())).then { throw TaskApiException(testData.exceptionMessage) }
        return GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
    }

    private fun customExceptionHandler(exceptions: MutableStateFlow<List<String>>) =
        CoroutineExceptionHandler { context, exception ->
            val exceptionMessage = exception.message ?: "unknown exception"
            exceptions.value = exceptions.value + listOf(exceptionMessage)
        }

    private fun waitForBoardCreation(model: IGameModel) = runTest {
        verify(mockReceiver, timeout(500)).didStateChange(State.UNINITIALIZED)
        verify(mockReceiver, timeout(500)).didStateChange(State.PREGAME)
        verify(mockReceiver, timeout(500)).didGridChange(any())
        model.requestStartBoard()
        verify(mockReceiver, timeout(500)).didStateChange(State.ACTIVE)
        verify(mockReceiver, timeout(500)).didGridChange(testData.boardTileGrid)
    }
}
