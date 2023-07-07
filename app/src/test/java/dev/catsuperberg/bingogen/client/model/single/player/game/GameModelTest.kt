package dev.catsuperberg.bingogen.client.model.single.player.game

import app.cash.turbine.test
import dev.catsuperberg.bingogen.client.common.DefaultTaskGrid
import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Grid.Companion.toGrid
import dev.catsuperberg.bingogen.client.common.MinuteAndSecondDurationFormatter
import dev.catsuperberg.bingogen.client.common.Task
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel
import dev.catsuperberg.bingogen.client.service.ITaskBoard
import dev.catsuperberg.bingogen.client.service.ITaskBoardFactory
import dev.catsuperberg.bingogen.client.service.ITaskRetriever
import dev.catsuperberg.bingogen.client.service.TaskApiException
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameModelReceiver
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameViewModel.TaskDetails
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.never
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameModelTest {
    private val testData = object {
        val exceptionMessage = "test"
        val selection = IGameModel.Selection("test", "sheet", 5)
        val grid = DefaultTaskGrid.grid
        val idToRequest = grid.indices.first
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

    @After
    fun tearDown() {
        clearInvocations(mockRetriever)
        clearInvocations(mockBoardFactory)
        clearInvocations(mockBoard)
    }

    @Test
    fun testHandlerCatchesUncaughtException() = runTest {
        val exceptions = MutableStateFlow<List<String>>(listOf())
        val handler = customExceptionHandler(exceptions)
        val scope = CoroutineScope(Job() + handler)

        whenever(mockBoardFactory.create(any(), anyOrNull())).then { throw Exception(testData.exceptionMessage) }

        GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory, scope)

        exceptions.test {
            skipItems(1)
            assertEquals(testData.exceptionMessage, awaitItem().first())
        }
    }

    @Test
    fun testBoardCreationDoesntFail() = runTest {
        val exceptions = MutableStateFlow<List<String>>(listOf())
        val handler = customExceptionHandler(exceptions)
        val scope = CoroutineScope(Job() + handler)

        GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory, scope)

        exceptions.test {
            skipItems(1)
            expectNoEvents()
        }
        verify(mockReceiver, never()).didModelFail(any())
    }

    @Test
    fun testInitAttachesBoardFlow() = runTest {
        GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        verify(mockReceiver, timeout(500)).attachBoardFlow(any())
    }

    @Test
    fun testInitCallsDidLoadBoardFailed() = runTest {
        initializeModelWithFailingBoardRetrieval()
        verify(mockReceiver, timeout(500)).didModelFail(testData.exceptionMessage)
    }

    @Test
    fun testInitAttachesDetailsFlow() = runTest {
        GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        verify(mockReceiver, timeout(500)).attachDetailsFlow(any())
    }

    @Test
    fun testInitAttachesBingoFlow() = runTest {
        GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        verify(mockReceiver, timeout(500)).attachBingoFlow(any())
    }

    @Test
    fun testFlowPassedToBingoFlowEmitsOnAnyBingo() = runTest {
        val bingoFlow = MutableStateFlow(false)
        val keptBingoFLow = MutableStateFlow(false)
        whenever(mockBoard.hasBingo).thenReturn(bingoFlow)
        whenever(mockBoard.hasKeptBingo).thenReturn(keptBingoFLow)

        val captor = argumentCaptor<StateFlow<Boolean>>()
        GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        verify(mockReceiver, timeout(500)).attachBingoFlow(captor.capture())
        val anyBingoFlow = captor.firstValue

        anyBingoFlow.test {
            skipItems(1)
            bingoFlow.value = true
            assertTrue(awaitItem())
            keptBingoFLow.value = true
            // stays true
            bingoFlow.value = false
            // stays true
            keptBingoFLow.value = false
            assertFalse(awaitItem())
            keptBingoFLow.value = true
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testSkippedAttachmentsWhenNoBoard() = runTest {
        initializeModelWithFailingBoardRetrieval()
        verify(mockReceiver, never()).attachBingoFlow(any())
        verify(mockReceiver, never()).attachBoardFlow(any())
    }

    @Test
    fun testDetailsRequestIgnoredWhenNoBoard() = runTest {
        val captor = argumentCaptor<StateFlow<TaskDetails?>>()
        val model = initializeModelWithFailingBoardRetrieval()
        verify(mockReceiver, timeout(500)).attachDetailsFlow(captor.capture())
        verify(mockReceiver, never()).attachBoardFlow(any())
        model.requestDetailsUpdates(testData.idToRequest)
        verifyNoMoreInteractions(mockBoard)
        assertNull(captor.firstValue.value)
    }

    @Test
    fun testRequestDetailsUpdates() = runTest {
        val boardFlow = MutableStateFlow(testData.grid)
        whenever(mockBoard.tasks).thenReturn(boardFlow)
        val captor = argumentCaptor<StateFlow<TaskDetails?>>()
        val durationFormatter = MinuteAndSecondDurationFormatter
        val model = GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory, timeLeftFormatter =  durationFormatter)
        waitForBoardCreation()
        verify(mockReceiver, timeout(500)).attachDetailsFlow(captor.capture())
        val detailsFlow = captor.firstValue

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
        val boardFlow = MutableStateFlow(testData.grid)
        whenever(mockBoard.tasks).thenReturn(boardFlow)
        val updateValue = { boardFlow.value = multiplyTimeToKeep(boardFlow, testData.idToRequest) }
        val captor = argumentCaptor<StateFlow<TaskDetails?>>()
        val model = GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        waitForBoardCreation()
        verify(mockReceiver, timeout(500)).attachDetailsFlow(captor.capture())
        val detailsFlow = captor.firstValue

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
    fun testToggleTaskDone() {
        val model = GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        waitForBoardCreation()
        model.toggleTaskDone(testData.idToRequest)
        verify(mockBoard, timeout(500)).toggleDone(testData.idToRequest, null)
        model.toggleTaskDone(testData.idToRequest, true)
        verify(mockBoard, timeout(500)).toggleDone(testData.idToRequest, true)
        model.toggleTaskDone(testData.idToRequest, false)
        verify(mockBoard, timeout(500)).toggleDone(testData.idToRequest, false)
    }

    @Test
    fun testToggleTaskTimer() {
        val model = GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        waitForBoardCreation()
        model.toggleTaskTimer(testData.idToRequest)
        verify(mockBoard, timeout(500)).toggleTaskTimer(testData.idToRequest, null)
        model.toggleTaskTimer(testData.idToRequest, true)
        verify(mockBoard, timeout(500)).toggleTaskTimer(testData.idToRequest, true)
        model.toggleTaskTimer(testData.idToRequest, false)
        verify(mockBoard, timeout(500)).toggleTaskTimer(testData.idToRequest, false)
    }

    @Test
    fun testToggleTaskKeptFromStart() {
        val model = GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        waitForBoardCreation()
        model.toggleTaskKeptFromStart(testData.idToRequest)
        verify(mockBoard, timeout(500)).toggleKeptFromStart(testData.idToRequest, null)
        model.toggleTaskKeptFromStart(testData.idToRequest, true)
        verify(mockBoard, timeout(500)).toggleKeptFromStart(testData.idToRequest, true)
        model.toggleTaskKeptFromStart(testData.idToRequest, false)
        verify(mockBoard, timeout(500)).toggleKeptFromStart(testData.idToRequest, false)
    }

    @Test
    fun testRestartTaskTimer() {
        val model = GameModel(testData.selection, mockReceiver, mockRetriever, mockBoardFactory)
        waitForBoardCreation()
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

    private fun waitForBoardCreation() = runTest {
        verify(mockReceiver, timeout(500)).attachBoardFlow(any())
    }
}
