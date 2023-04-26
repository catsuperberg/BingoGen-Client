package dev.catsuperberg.bingogen.client.view.model.common.gamesetup

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dev.catsuperberg.bingogen.client.model.interfaces.IGameSetupModel

class GameSetupViewModel(
    private val navCallbacks: IGameSetupViewModel.NavCallbacks,
    private val state: IGameSetupState,
    private val model: IGameSetupModel,
) : ViewModel(), IGameSetupViewModel {

    override val gameSelection: State<List<String>> = state.gameSelection
    override val sheetSelection: State<List<String>> = state.sheetSelection

    init {
        model.requestGameList()
        model.requestSheetList()
    }

    override fun requestBack() {
        navCallbacks.onBack()
    }

    override fun requestSetupDone() {
        navCallbacks.onStartGame(gameSelection.value.random(), sheetSelection.value.random())
    }
}

class GameSetupState(): IGameSetupState {
    override val gameSelection: MutableState<List<String>> = mutableStateOf(listOf())
    override val sheetSelection: MutableState<List<String>> = mutableStateOf(listOf())

    override fun didLoadGames(games: List<String>) {
        gameSelection.value = gameSelection.value + games
    }

    override fun didLoadSheets(sheets: List<String>) {
        sheetSelection.value = sheetSelection.value + sheets
    }
}
