package dev.catsuperberg.bingogen.client.view.model.start

import androidx.compose.runtime.State

interface IStartFields {
    val serverList: State<List<String>>
    val selectedServerIndex: State<Int?>
}

interface IStartRequests {
    fun requestServerChange(index: Int)
    fun requestSinglePlayer()
    fun requestMultiplayer()
}


interface IStartViewModel : IStartFields, IStartRequests {
    data class NavCallbacks(
        val onSinglePlayer: () -> Unit,
        val onMultiplayer: () -> Unit,
    )
}

interface IStartModelReceiver {
    fun didLoadServers(servers: List<String>, selected: Int)
}

interface IStartState : IStartFields, IStartModelReceiver {
    fun setSelectedServer(index: Int)
}
