package dev.catsuperberg.bingogen.client.common

import dev.catsuperberg.bingogen.client.common.ServerAddress.Companion.toServerAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ServerAddressTest {
    @Test
    fun testValid() {
        assertTrue(ServerAddress.valid("example.com:80"))
        assertTrue(ServerAddress.valid("127.0.0.1:80"))
        assertFalse(ServerAddress.valid("http://example.com:80"))
        assertFalse(ServerAddress.valid("example.com"))
        assertFalse(ServerAddress.valid("example.com:"))
        assertFalse(ServerAddress.valid(":80"))
    }

    @Test
    fun testToServerAddress() {
        val validString = "example.com:80"
        val invalidString = "http://example.com:80"

        val serverAddress = validString.toServerAddress()
        assertEquals(serverAddress.url, "example.com")
        assertEquals(serverAddress.port, "80")

        try {
            invalidString.toServerAddress()
            fail("Expected IllegalArgumentException to be thrown")
        } catch (e: IllegalArgumentException) {
            assertEquals(e.message, "Invalid server string format")
        }
    }
}
