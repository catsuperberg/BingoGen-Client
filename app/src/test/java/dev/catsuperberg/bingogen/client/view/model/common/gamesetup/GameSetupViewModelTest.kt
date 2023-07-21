package dev.catsuperberg.bingogen.client.view.model.common.gamesetup

import dev.catsuperberg.bingogen.client.model.interfaces.IGameSetupModel
import dev.catsuperberg.bingogen.client.view.model.common.gamesetup.IGameSetupState.Direction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

class GameSetupViewModelTest {
    private val testGames = listOf("Game 1", "Game 2", "Game 3", "Game 4")
    private val testSheets = listOf("Sheet 1", "Sheet 2", "Sheet 3", "Sheet 4")

    @Mock private lateinit var mockCallbacks: IGameSetupViewModel.NavCallbacks
    @Mock private lateinit var mockState: IGameSetupState
    @Mock private lateinit var mockModel: IGameSetupModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Mockito.`when`(mockCallbacks.onStartGame).thenReturn(mock())
        Mockito.`when`(mockCallbacks.onBack).thenReturn(mock())

        Dispatchers.setMain(StandardTestDispatcher())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitRequestsGameList() {
        createViewModelWithMocks()
        verify(mockModel).requestGameList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testGameSelectedOnFirstLoad() = runTest {
        val gameFlow: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
        Mockito.`when`(mockState.sheetSelection).thenReturn(MutableStateFlow(listOf()))
        Mockito.`when`(mockState.gameSelection).thenReturn(gameFlow)

        Mockito.`when`(mockModel.requestGameList()).then { gameFlow.value = testGames; Unit }
        createViewModelWithMocks()
        advanceUntilIdle()
        gameFlow.value = testGames + listOf("additional game", "this emission should be ignored")
        advanceUntilIdle()

        verify(mockState, times(1)).setChosenGame(0)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testSheetSelectedOnAnyGameChange() = runTest {
        var callCounter = 0
        val gameFlow: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
        val sheetFlow: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
        Mockito.`when`(mockState.gameSelection).thenReturn(gameFlow)
        Mockito.`when`(mockState.sheetSelection).thenReturn(sheetFlow)

        Mockito.`when`(mockModel.requestSheetList(any())).then { sheetFlow.value = sheetFlow.value + testSheets; Unit }
        val vm = createViewModelWithMocks()
        gameFlow.value = testGames
        advanceUntilIdle()
        verify(mockState, times(++callCounter)).setChosenSheet(0)

        val gamesToSwitchTo = testGames.indices.filter { it != 0 }
        gamesToSwitchTo.forEach { index ->
            vm.onGameChange(index)
            advanceUntilIdle()
            verify(mockState, times(++callCounter)).setChosenSheet(0)
        }
    }

    @Test
    fun testOnBack() {
        val vm = createViewModelWithMocks()
        vm.onBack()
        verify(mockCallbacks.onBack)()
    }

    @Test
    fun testOnGameChangeSetsCorrectChoice() {
        Mockito.`when`(mockState.gameSelection).thenReturn(MutableStateFlow(testGames))
        val firstIndex = 0
        val secondIndex = 1
        val lastIndex = testGames.lastIndex
        val vm = createViewModelWithMocks()
        vm.onGameChange(firstIndex)
        vm.onGameChange(secondIndex)
        vm.onGameChange(lastIndex)

        val captor = argumentCaptor<Int>()
        verify(mockState, times(3)).setChosenGame(captor.capture())
        assertEquals(firstIndex, captor.allValues[0])
        assertEquals(secondIndex, captor.allValues[1])
        assertEquals(lastIndex, captor.allValues[2])
    }

    @Test
    fun testOnGameChangeRequestsSheetList() {
        val gameAt = testGames.lastIndex
        val expectedGame = testGames[gameAt]
        Mockito.`when`(mockState.gameSelection).thenReturn(MutableStateFlow(testGames))
        val vm = createViewModelWithMocks()
        vm.onGameChange(gameAt)
        verify(mockModel).requestSheetList(eq(expectedGame))
    }

    @Test
    fun testInvalidOnGameChangeIgnored() {
        val invalidIndex = -1
        val vm = createViewModelWithMocks()
        vm.onGameChange(invalidIndex)
        verify(mockState, never()).setChosenGame(any())
    }

    @Test
    fun testOnSheetChange() {
        Mockito.`when`(mockState.sheetSelection).thenReturn(MutableStateFlow(testSheets))
        val firstIndex = 0
        val secondIndex = 1
        val lastIndex = testGames.lastIndex
        val vm = createViewModelWithMocks()
        vm.onSheetChange(firstIndex)
        vm.onSheetChange(secondIndex)
        vm.onSheetChange(lastIndex)

        val captor = argumentCaptor<Int>()
        verify(mockState, times(3)).setChosenSheet(captor.capture())
        assertEquals(firstIndex, captor.allValues[0])
        assertEquals(secondIndex, captor.allValues[1])
        assertEquals(lastIndex, captor.allValues[2])
    }

    @Test
    fun testInvalidOnSheetChangeIgnored() {
        val invalidIndex = -1
        val vm = createViewModelWithMocks()
        vm.onSheetChange(invalidIndex)
        verify(mockState, never()).setChosenSheet(any())
    }

    @Test
    fun testOnSizeUp() {
        val vm = createViewModelWithMocks()
        vm.onSizeUp()
        verify(mockState).incrementSideCount(Direction.UP)
    }

    @Test
    fun testOnSizeDown() {
        val vm = createViewModelWithMocks()
        vm.onSizeDown()
        verify(mockState).incrementSideCount(Direction.DOWN)
    }

    @Test
    fun testOnDone() {
        val game = 0
        val sheet = 0
        val sideCount = 5
        Mockito.`when`(mockState.gameSelection).thenReturn(MutableStateFlow(testGames))
        Mockito.`when`(mockState.sheetSelection).thenReturn(MutableStateFlow(testSheets))
        Mockito.`when`(mockState.chosenGame).thenReturn(MutableStateFlow(game))
        Mockito.`when`(mockState.chosenSheet).thenReturn(MutableStateFlow(sheet))
        Mockito.`when`(mockState.boardSideCount).thenReturn(MutableStateFlow(sideCount))
        val vm = createViewModelWithMocks()
        vm.onDone()
        verify(mockCallbacks.onStartGame)(eq(testGames[game]), eq(testSheets[sheet]), eq(sideCount))
    }

    @Test
    fun testIgnoreOnDoneIfNoValues() {
        val game = 0
        val sheet = 0
        val sideCount = 5
        Mockito.`when`(mockState.gameSelection).thenReturn(MutableStateFlow(testGames))
        Mockito.`when`(mockState.sheetSelection).thenReturn(MutableStateFlow(testSheets))
        Mockito.`when`(mockState.boardSideCount).thenReturn(MutableStateFlow(sideCount))
        val vm = createViewModelWithMocks()

        Mockito.`when`(mockState.chosenGame).thenReturn(MutableStateFlow(null))
        Mockito.`when`(mockState.chosenSheet).thenReturn(MutableStateFlow(sheet))
        vm.onDone()

        Mockito.`when`(mockState.chosenGame).thenReturn(MutableStateFlow(game))
        Mockito.`when`(mockState.chosenSheet).thenReturn(MutableStateFlow(null))
        vm.onDone()

        Mockito.`when`(mockState.chosenGame).thenReturn(MutableStateFlow(null))
        Mockito.`when`(mockState.chosenSheet).thenReturn(MutableStateFlow(null))
        vm.onDone()

        verify(mockCallbacks.onStartGame, never())(any(), any(), any())
    }

    private fun createViewModelWithMocks() = GameSetupViewModel(
        navCallbacks = mockCallbacks,
        state = mockState,
        model = mockModel,
    )
}
