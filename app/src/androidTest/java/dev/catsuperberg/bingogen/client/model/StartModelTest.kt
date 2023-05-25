package dev.catsuperberg.bingogen.client.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import dev.catsuperberg.bingogen.client.Credentials
import dev.catsuperberg.bingogen.client.KoinTestRule
import dev.catsuperberg.bingogen.client.model.start.StartModel
import dev.catsuperberg.bingogen.client.testDataStore
import dev.catsuperberg.bingogen.client.view.model.start.IStartState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.mockito.kotlin.eq
import kotlin.time.Duration.Companion.milliseconds

@RunWith(AndroidJUnit4::class)
class StartModelTest : KoinComponent {
    private val instrumentedTestModule = module {
        single { get<Context>().testDataStore }
    }

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(instrumentedTestModule)
    )

    private val dataStore: DataStore<Credentials> by inject()
    private val mockReceiver = mock(IStartState::class.java)
    private val testServers = listOf("127.0.0.1:8080", "192.168.1.1:9999","0.0.0.0:1111",)

    @Before
    fun setup() {

    }

    @After
    fun teardown() {
        runBlocking { dataStore.updateData { it.toBuilder().clear().build() } }
    }

    @Test
    fun testEmitsEmptyListOnEmptyDataStore() {
        StartModel(mockReceiver, dataStore)
        verify(mockReceiver, timeout(500)).didStoredServersChange(eq(listOf()))
    }

    @Test
    fun testReceiverGetsDataOnInit() {
        setDatastoreServers(testServers)

        StartModel(mockReceiver, dataStore)
        verify(mockReceiver, timeout(500)).didStoredServersChange(eq(testServers))
    }

    @Test
    fun testSaveServers() = runBlocking {
        dataStore.data
            .map { credentials -> credentials.serversList }
            .test(timeout = 500.milliseconds) {
                skipItems(1) // dataStore.data emits on subscription so need to skip empty value
                val model = StartModel(mockReceiver, dataStore)
                model.saveServers(testServers)
                val result = awaitItem()
                assertEquals(testServers, result)
            }
    }

    private fun setDatastoreServers(servers: List<String>) {
        runBlocking {
            dataStore.updateData { current ->
                current.toBuilder()
                    .clearServers()
                    .addAllServers(servers)
                    .build()
            }
        }
    }
}
