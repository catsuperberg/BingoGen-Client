package dev.catsuperberg.bingogen.client.view.model.start

import dev.catsuperberg.bingogen.client.common.ServerAddress
import kotlinx.coroutines.flow.StateFlow

interface IStartFields {
    val serverList: StateFlow<List<String>>
    val serverString: StateFlow<String>
    val indicateBadInput: StateFlow<Boolean>
}

interface IStartRequests {
    fun onServerStringChange(value: String)
    fun onSelectServerFromList(index: Int)
    fun onDeleteServer(index: Int)
    fun onSinglePlayer()
    fun onMultiplayer()
}


interface IStartViewModel : IStartRequests {
    val state: IStartState
    data class NavCallbacks(
        val onSinglePlayer: (server: ServerAddress) -> Unit,
        val onMultiplayer: (server: ServerAddress) -> Unit,
    )
}

interface IStartModelReceiver {
    fun didStoredServersChange(servers: List<String>)
}

interface IStartState : IStartFields, IStartModelReceiver {
    fun setInputToServer(index: Int)
    fun setIndicateBadInput(value: Boolean)
    fun setServerString(string: String)
}
