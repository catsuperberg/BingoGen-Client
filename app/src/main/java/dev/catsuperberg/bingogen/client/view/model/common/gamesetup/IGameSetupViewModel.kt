package dev.catsuperberg.bingogen.client.view.model.common.gamesetup

import androidx.compose.runtime.State

interface IGameSetupFields {
    val gameSelection: State<List<String>>
    val sheetSelection: State<List<String>>
}

interface IGameSetupRequests {
    fun requestBack()
    fun requestSetupDone()
}

interface IGameSetupViewModel : IGameSetupFields, IGameSetupRequests {
    data class NavCallbacks(
        val onStartGame: (game: String, sheet: String) -> Unit,
        val onBack: () -> Unit,
    )
}

interface IGameSetupModelReceiver {
    fun didLoadGames(games: List<String>)
    fun didLoadSheets(sheets: List<String>)
}

interface IGameSetupState : IGameSetupFields, IGameSetupModelReceiver
