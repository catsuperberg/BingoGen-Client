package dev.catsuperberg.bingogen.client.view.model.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.catsuperberg.bingogen.client.common.ServerAddress
import dev.catsuperberg.bingogen.client.common.ServerAddress.Companion.toServerAddress
import dev.catsuperberg.bingogen.client.model.start.IStartModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class StartViewModel(
    navCallbacks: IStartViewModel.NavCallbacks,
    override val state: IStartState,
    private val model: IStartModel,
) : ViewModel(), IStartViewModel {
    // FIXME Needed as appyx doesn't support ViewModel as ViewModelStoreOwner onCleared() isn't called
    private val navCallbacks = IStartViewModel.NavCallbacks(
        onSinglePlayer = { server ->
            stopModelScope()
            navCallbacks.onSinglePlayer(server)
         },
        onMultiplayer = { server ->
            stopModelScope()
            navCallbacks.onMultiplayer(server)
        },
    )

    init {
        setInputToTopServerOnFirstValidList()
    }

    private fun setInputToTopServerOnFirstValidList() {
        viewModelScope.launch {
            state.serverList.collect { servers ->
                if (servers.isNotEmpty()) {
                    state.setInputToServer(0)
                    cancel()
                }
            }
        }
    }

    override fun onServerStringChange(value: String) {
        state.setIndicateBadInput(ServerAddress.valid(value).not())
        state.setServerString(value)
    }

    override fun onSelectServerFromList(index: Int) {
        if (index !in state.serverList.value.indices) {
            assert(true) { "Index of a server requested for selection is outside the range" }
            return
        }
        state.setInputToServer(index)
    }

    override fun onDeleteServer(index: Int) {
        if (index !in state.serverList.value.indices) {
            assert(true) { "Index of a server requested for deletion is outside the range" }
            return
        }

        val newServers = state.serverList.value.filterIndexed { i, _ -> i != index }
        model.saveServers(newServers)
    }

    override fun onSinglePlayer() {
        state.setIndicateBadInput(ServerAddress.valid(state.serverString.value).not())
        if (state.indicateBadInput.value)
            return
        setServerStringAsTop()
        navCallbacks.onSinglePlayer(state.serverString.value.toServerAddress())
    }

    override fun onMultiplayer() {
        state.setIndicateBadInput(ServerAddress.valid(state.serverString.value).not())
        if (state.indicateBadInput.value)
            return
        setServerStringAsTop()
        navCallbacks.onMultiplayer(state.serverString.value.toServerAddress())
    }

    private fun setServerStringAsTop() {
        val strippedList = state.serverList.value.filter { it != state.serverString.value }
        val newServers = listOf(state.serverString.value) + strippedList
        model.saveServers(newServers)
    }

    // FIXME Due to appyx not supporting ViewModel as ViewModelStoreOwner onCleared() isn't called
    override fun onCleared() {
        super.onCleared()
        stopModelScope()
    }

    private fun stopModelScope() {
        model.close()
    }
}

class StartState() : IStartState {
    override val serverList: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    override val serverString: MutableStateFlow<String> = MutableStateFlow("")
    override val indicateBadInput: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override fun didStoredServersChange(servers: List<String>) {
        serverList.value = servers
    }

    override fun setInputToServer(index: Int) {
        serverString.value = serverList.value[index]
    }

    override fun setIndicateBadInput(value: Boolean) {
        indicateBadInput.value = value
    }

    override fun setServerString(string: String) {
        serverString.value = string
    }
}
