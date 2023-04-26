package dev.catsuperberg.bingogen.client.view.model.start

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dev.catsuperberg.bingogen.client.model.start.IStartModel

class StartViewModel(
    private val navCallbacks: IStartViewModel.NavCallbacks,
    private val state: IStartState,
    private val model: IStartModel,
) : ViewModel(), IStartViewModel {
    override val serverList: State<List<String>> = state.serverList
    override val selectedServerIndex: State<Int?> = state.selectedServerIndex

    init {
        model.requestServers()
    }

    override fun requestServerChange(index: Int) {
        state.setSelectedServer(index)
    }

    override fun requestSinglePlayer() {
        navCallbacks.onSinglePlayer()
    }

    override fun requestMultiplayer() {
        navCallbacks.onMultiplayer()
    }
}

class StartState : IStartState {
    override val serverList: MutableState<List<String>> = mutableStateOf(listOf())
    override val selectedServerIndex: MutableState<Int?> = mutableStateOf(null)

    override fun didLoadServers(servers: List<String>, selected: Int) {
        serverList.value = servers
        selectedServerIndex.value = selected
    }

    override fun setSelectedServer(index: Int) {
        if(index in serverList.value.indices)
            selectedServerIndex.value = index
    }
}
