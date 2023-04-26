package dev.catsuperberg.bingogen.client.view.model.multiplayer.lobby.selector

interface ILobbySelectorRequests {
    fun requestSetup()
    fun requestBack()
}

interface ILobbySelectorViewModel : ILobbySelectorRequests {
    data class NavCallbacks(
        val onGameSetupModel: () -> Unit,
        val onBack: () -> Unit,
    )
}
