package dev.catsuperberg.bingogen.client.view.model.multiplayer.lobby.selector

import androidx.lifecycle.ViewModel

class LobbySelectorViewModel(
    private val navCallbacks: ILobbySelectorViewModel.NavCallbacks
) : ViewModel(), ILobbySelectorViewModel {
    override fun requestSetup() {
        navCallbacks.onGameSetupModel()
    }

    override fun requestBack() {
        navCallbacks.onBack()
    }
}
