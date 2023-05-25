package dev.catsuperberg.bingogen.client.view.model.start

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StartStateTest {
    @Test
    fun testServerList() {
        val state = StartState()
        state.didStoredServersChange(listOf("server1", "server2"))
        assertEquals(state.serverList.value, listOf("server1", "server2"))
    }

    @Test
    fun testSetInputToServer() {
        val state = StartState()
        state.didStoredServersChange(listOf("server1", "server2"))
        state.setInputToServer(1)
        assertEquals(state.serverString.value, "server2")
    }

    @Test
    fun testSetIndicateBadInput() {
        val state = StartState()
        state.setIndicateBadInput(true)
        assertTrue(state.indicateBadInput.value)
    }

    @Test
    fun testSetServerString() {
        val state = StartState()
        state.setServerString("new server")
        assertEquals(state.serverString.value, "new server")
    }
}
