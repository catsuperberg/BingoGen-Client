package dev.catsuperberg.bingogen.client.view.model.common.gamesetup

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface IGameSetupFields {
    val gameSelection: StateFlow<List<String>>
    val chosenGame: StateFlow<Int?>
    val sheetSelection: StateFlow<List<String>>
    val chosenSheet: StateFlow<Int?>
    val boardSideCount: StateFlow<Int>
    val snackBarMessage: SharedFlow<String>
}

interface IGameSetupRequests {
    fun onBack()
    fun onGameChange(index: Int)
    fun onSheetChange(index: Int)
    fun onSizeUp()
    fun onSizeDown()
    fun onDone()
}

interface IGameSetupViewModel : IGameSetupRequests {
    val state: IGameSetupFields
    data class NavCallbacks(
        val onStartGame: (game: String, sheet: String, sideCount: Int) -> Unit,
        val onBack: () -> Unit,
    )
}

interface IGameSetupModelReceiver {
    fun didLoadGames(games: List<String>)
    fun didLoadSheets(sheets: List<String>)
    suspend fun didServerCallFailed(message: String)
}

interface IGameSetupState : IGameSetupFields, IGameSetupModelReceiver {
    enum class Direction(val sign: Int) { UP(1), DOWN(-1) }
    fun setChosenGame(index: Int)
    fun setChosenSheet(index: Int)
    fun incrementSideCount(direction: Direction)
}
