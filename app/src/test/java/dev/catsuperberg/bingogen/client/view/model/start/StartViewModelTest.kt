package dev.catsuperberg.bingogen.client.view.model.start

import dev.catsuperberg.bingogen.client.common.ServerAddress
import dev.catsuperberg.bingogen.client.model.start.IStartModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

class StartViewModelTest {
    companion object {
        private val testServers = listOf("127.0.0.1:8080", "192.168.1.1:9999", "0.0.0.0:1111",)
        private val testServersWithSecondAsFirst = listOf("192.168.1.1:9999", "127.0.0.1:8080", "0.0.0.0:1111",)
        private val testServersWithoutSecond = listOf("127.0.0.1:8080", "0.0.0.0:1111",)
    }

    @Mock lateinit var mockCallbacks: IStartViewModel.NavCallbacks
    @Mock lateinit var mockState: IStartState
    @Mock lateinit var mockModel: IStartModel


    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Mockito.`when`(mockState.serverList).thenReturn(MutableStateFlow(listOf()))
        val mockCallback: (ServerAddress) -> Unit = mock()
        doReturn(mockCallback).`when`(mockCallbacks).onSinglePlayer
        doReturn(mockCallback).`when`(mockCallbacks).onMultiplayer
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testNoInitialServerStringOnEmptyList() {
        createViewModelWithMocks()
        verify(mockState).serverList
        verifyNoMoreInteractions(mockState)
    }

    @Test
    fun testInitialServerStringFirstFromList() {
        Mockito.`when`(mockState.serverList).thenReturn(MutableStateFlow(testServers))
        createViewModelWithMocks()
        verify(mockState).setInputToServer(0)
    }

    @Test
    fun testOnServerStringChange() {
        val vm = createViewModelWithMocks()
        val text = testServers.first()
        vm.onServerStringChange(text)
        verify(mockState).setServerString(text)
    }

    @Test
    fun testBadInputIndicated() {
        val validString = testServers.first()
        val invalidString = "bonjour"
        val vm = createViewModelWithMocks()
        val changeAndVerify = { string: String, result: Boolean ->
            vm.onServerStringChange(string)
            verify(mockState).setIndicateBadInput(result)
        }
        changeAndVerify(validString, false)
        changeAndVerify(invalidString, true)
    }

    @Test
    fun testOnSelectServerFromList() {
        val vm = createViewModelWithMocks()
        Mockito.`when`(mockState.serverList).thenReturn(MutableStateFlow(testServers)) // after vm creation to skip the setInputToServer call by init
        testServers.forEachIndexed { index, _ ->
            vm.onSelectServerFromList(index)
            verify(mockState).setInputToServer(index)
        }
    }

    @Test
    fun testOnDeleteServer() {
        val vm = createViewModelWithMocks()
        Mockito.`when`(mockState.serverList).thenReturn(MutableStateFlow(testServers))
        vm.onDeleteServer(1)
        verify(mockModel).saveServers(testServersWithoutSecond)
    }

    @Test
    fun testOnSinglePlayer() {
        val vm = createViewModelWithMocks()
        val call = { vm.onSinglePlayer() }
        val mockProperty = mockCallbacks::onSinglePlayer
        testNavigationVariant(vm, call, mockProperty)
    }

    @Test
    fun testOnMultiplayer() {
        val vm = createViewModelWithMocks()
        val call = { vm.onMultiplayer() }
        val mockProperty = mockCallbacks::onMultiplayer
        testNavigationVariant(vm, call, mockProperty)
    }

    private fun testNavigationVariant(
        vm: IStartViewModel,
        vmMethodCall: () -> Unit,
        mockPropertyToCheck: KProperty<(ServerAddress) -> Unit>,
    ) {
        val serverAddressCaptor = argumentCaptor<ServerAddress>()
        Mockito.`when`(mockState.serverList).thenReturn(MutableStateFlow(testServers))
        Mockito.`when`(mockState.indicateBadInput).thenReturn(MutableStateFlow(false))
        testServers.forEachIndexed { index, originalString ->
            Mockito.`when`(mockState.serverString).thenReturn(MutableStateFlow(originalString))
            vm.onSelectServerFromList(index)
            vmMethodCall()
        }

        verify(mockPropertyToCheck.call(), times(testServers.size))(serverAddressCaptor.capture())
        val results = serverAddressCaptor.allValues
            .map { "${it.url}:${it.port}" }
        assertEquals(testServers, results)
    }

    @Test
    fun testOnSinglePlayerIgnoreOnBadInput() {
        val vm = createViewModelWithMocks()
        verifyNoNavCallOnRequestWithBadInput(vm::onSinglePlayer)
    }

    @Test
    fun testOnMultiplayerPlayerIgnoreOnBadInput() {
        val vm = createViewModelWithMocks()
        verifyNoNavCallOnRequestWithBadInput(vm::onMultiplayer)
    }

    private fun verifyNoNavCallOnRequestWithBadInput(requestCall: KFunction<Unit>) {
        Mockito.`when`(mockState.serverString).thenReturn(MutableStateFlow("invalid"))
        Mockito.`when`(mockState.indicateBadInput).thenReturn(MutableStateFlow(true))
        requestCall.call()
        verifyNoMoreInteractions(mockCallbacks)
    }

    @Test
    fun testOnSinglePlayerPushToTop() {
        verifyOnCallServerPushToTop {
            val vm = createViewModelWithMocks()
            vm.onSinglePlayer()
        }
    }

    @Test
    fun testOnMultiplayerPushToTop() {
        verifyOnCallServerPushToTop {
            val vm = createViewModelWithMocks()
            vm.onMultiplayer()
        }
    }

    private fun verifyOnCallServerPushToTop(requestCall: () -> Unit) {
        val additionalServer = "255.255.255.255:65535"
        val expectedWithAddition = listOf(additionalServer) + testServers

        Mockito.`when`(mockState.serverList).thenReturn(MutableStateFlow(testServers))
        Mockito.`when`(mockState.indicateBadInput).thenReturn(MutableStateFlow(false))

        Mockito.`when`(mockState.serverString).thenReturn(MutableStateFlow(testServers.first()))
        requestCall()
        verify(mockModel).saveServers(eq(testServers))

        Mockito.`when`(mockState.serverString).thenReturn(MutableStateFlow(testServersWithSecondAsFirst.first()))
        requestCall()
        verify(mockModel).saveServers(eq(testServersWithSecondAsFirst))


        Mockito.`when`(mockState.serverString).thenReturn(MutableStateFlow(additionalServer))
        requestCall()
        verify(mockModel).saveServers(eq(expectedWithAddition))
    }

    private fun createViewModelWithMocks() = StartViewModel(
        navCallbacks = mockCallbacks,
        state = mockState,
        model = mockModel,
    )
}
