package dev.catsuperberg.bingogen.client.service

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dev.catsuperberg.bingogen.client.api.GridMapper
import dev.catsuperberg.bingogen.client.api.IGridMapper
import dev.catsuperberg.bingogen.client.api.TaskMapper
import dev.catsuperberg.bingogen.client.service.test.data.TestServerResponses
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import retrofit2.Retrofit

class TaskRetrieverTest : KoinTest {
    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(module {
            single { MockWebServer().apply { start() } }
            single<Retrofit> {
                val server: MockWebServer = get()
                val contentType: MediaType = "application/json".toMediaType()

                Retrofit.Builder().baseUrl(server.url("/"))
                    .addConverterFactory(Json.asConverterFactory(contentType)).build()
            }
            single<IGridMapper> { GridMapper(TaskMapper()) }
            single<ITaskRetriever> { TaskRetriever(get(), get()) }
        })
    }

    private val mockWebServer: MockWebServer by inject()
    private val retriever: ITaskRetriever by inject()

    @Test
    fun getGamesTest() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(TestServerResponses.Game.body)
        )
        val result = retriever.getGames()
        assertEquals(TestServerResponses.Game.expected, result)
    }

    @Test
    fun getSheetsTest() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(TestServerResponses.Sheets.body)
        )
        val result = retriever.getSheets("Test")
        assertEquals(TestServerResponses.Sheets.expected, result)
    }

    @Test
    fun getBoardTest() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(TestServerResponses.Board.body)
        )
        val result = retriever.getBoard(2, "Test", "Test")
        assertEquals(TestServerResponses.Board.expected, result)
    }

    @Test
    fun throwsOnIllegalSideCount() {
        assertThrows(IllegalArgumentException::class.java) { retriever.getBoard(1, "Test", "Test") }
        assertThrows(IllegalArgumentException::class.java) { retriever.getBoard(0, "Test", "Test") }
        assertThrows(IllegalArgumentException::class.java) { retriever.getBoard(-1, "Test", "Test") }
    }

    @Test
    fun throwsIfUnSuccessfulTest() {
        TestServerResponses.unsuccessfulStatusCodes.forEach(::assertThrowsOnCode)
    }

    private fun assertThrowsOnCode(code: Int) {
        val assertions = listOf<() -> Unit>(
            { retriever.getGames() },
            { retriever.getSheets("Test") },
            { retriever.getBoard(2, "Test", "Test") },
        )
        assertions.forEach {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(code)
            )
            assertThrows(TaskApiException::class.java, it)
        }
    }
}
