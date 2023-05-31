package dev.catsuperberg.bingogen.client.model.single.player.gamesetup

import dev.catsuperberg.bingogen.client.service.ITaskRetriever
import dev.catsuperberg.bingogen.client.service.TaskApiException
import dev.catsuperberg.bingogen.client.view.model.common.gamesetup.IGameSetupModelReceiver
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify

class GameSetupModelTest {
    private val testGames = listOf("Game 1", "Game 2", "Game 3", "Game 4", "Game 5")
    private val testGame1Sheets = listOf("Sheet 1", "Sheet 2")
    private val testGame2Sheets = listOf("Sheet 3", "Sheet 4")
    private val testGame3Sheets = listOf("Sheet 5", "Sheet 6")
    private val testGame4Sheets = listOf("Sheet 7", "Sheet 8")
    private val testGame5Sheets = listOf("Sheet 9", "Sheet 10")

    @Mock private lateinit var mockReceiver: IGameSetupModelReceiver
    @Mock private lateinit var mockRetriever: ITaskRetriever

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testRequestGameList() {
        Mockito.`when`(mockRetriever.getGames()).thenReturn(testGames)
        val model = GameSetupModel(mockReceiver, mockRetriever)
        model.requestGameList()
        verify(mockReceiver, timeout(200)).didLoadGames(eq(testGames))
        model.close()
    }

    @Test
    fun testRequestSheetList() {
        Mockito.`when`(mockRetriever.getSheets(testGames[0])).thenReturn(testGame1Sheets)
        val model = GameSetupModel(mockReceiver, mockRetriever)
        model.requestSheetList(testGames[0])
        verify(mockReceiver, timeout(200)).didLoadSheets(eq(testGame1Sheets))
        model.close()
    }

    @Test
    fun testFailedGameRequest() = runBlocking {
        Mockito.`when`(mockRetriever.getGames()).doAnswer { throw TaskApiException("") }
        val model = GameSetupModel(mockReceiver, mockRetriever)
        model.requestGameList()
        verify(mockReceiver, timeout(200)).didServerCallFailed(any())
        model.close()
    }

    @Test
    fun testFailedSheetRequest() = runBlocking {
        Mockito.`when`(mockRetriever.getSheets(any())).doAnswer { throw TaskApiException("") }
        val model = GameSetupModel(mockReceiver, mockRetriever)
        model.requestSheetList("")
        verify(mockReceiver, timeout(200)).didServerCallFailed(any())
        model.close()
    }
}
