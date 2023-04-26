package dev.catsuperberg.bingogen.client.view.model.common.game

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel

class GameViewModel(
    selection: IGameViewModel.Selection,
    private val navCallbacks: IGameViewModel.NavCallbacks,
    private val state: IGameState,
    private val model: IGameModel,
) : ViewModel(), IGameViewModel {

    override val selectedGame: State<String> = state.selectedGame
    override val selectedSheet: State<String> = state.selectedSheet
    override val board: State<List<String>> = state.board

    init {
        model.requestBoard()
        state.setGame(selection.game)
        state.setSheet(selection.sheet)
    }

    override fun requestBack() {
        navCallbacks.onBack()
    }
}

class GameState(): IGameState {
    override val selectedGame: MutableState<String> = mutableStateOf("")
    override val selectedSheet: MutableState<String> = mutableStateOf("")
    override val board: MutableState<List<String>> = mutableStateOf(listOf(""))

    override fun didLoadBoard(board: List<List<String>>) {
        this.board.value = board.flatten()
    }

    override fun setGame(game: String) {
        selectedGame.value = game
    }

    override fun setSheet(sheet: String) {
        selectedSheet.value = sheet
    }
}
