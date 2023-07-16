package dev.catsuperberg.bingogen.client.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServerAddress(val url: String, val port: String) : Parcelable {
    companion object {
        private val serverStringRegex = Regex("""^[A-Za-z0-9\-\.]+:[0-9]+$""")

        fun valid(server: String) = serverStringRegex.matches(server)
        fun String.toServerAddress(): ServerAddress {
            require(valid(this)) { "Invalid server string format" }
            return this.split(":").let { ServerAddress(it[0], it[1]) }
        }
    }

    override fun toString() = "$url:$port"
}
