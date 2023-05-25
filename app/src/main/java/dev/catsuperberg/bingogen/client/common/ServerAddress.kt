package dev.catsuperberg.bingogen.client.common

data class ServerAddress(val url: String, val port: String) {
    companion object {
        private val serverStringRegex = Regex("""^[A-Za-z0-9\-\.]+:[0-9]+$""")

        fun valid(server: String) = serverStringRegex.matches(server)
        fun String.toServerAddress(): ServerAddress {
            require(valid(this)) { "Invalid server string format" }
            return this.split(":").let { ServerAddress(it[0], it[1]) }
        }
    }
}
