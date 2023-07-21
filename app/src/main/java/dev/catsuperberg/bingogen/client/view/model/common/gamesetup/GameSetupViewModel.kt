package dev.catsuperberg.bingogen.client.view.model.common.gamesetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.catsuperberg.bingogen.client.model.interfaces.IGameSetupModel
import dev.catsuperberg.bingogen.client.view.model.common.gamesetup.IGameSetupState.Direction
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GameSetupViewModel(
    navCallbacks: IGameSetupViewModel.NavCallbacks,
    override val state: IGameSetupState,
    private val model: IGameSetupModel,
) : ViewModel(), IGameSetupViewModel {
    // FIXME Needed as appyx doesn't support ViewModel as ViewModelStoreOwner onCleared() isn't called
    private val navCallbacks = IGameSetupViewModel.NavCallbacks(
        onStartGame = { game: String, sheet: String, sideCount: Int ->
            stopModelScope()
            navCallbacks.onStartGame(game, sheet, sideCount)
        },
        onBack = {
            stopModelScope()
            navCallbacks.onBack()
        },
    )

    init {
        selectGameOnFirstSelection()
        startSelectSheetOnGameChange()
        model.requestGameList()
    }

    private fun selectGameOnFirstSelection() {
        viewModelScope.launch {
            state.gameSelection.collect { selection ->
                if (selection.isNotEmpty()) {
                    onGameChange(0)
                    cancel()
                }
            }
        }
    }

    private fun startSelectSheetOnGameChange() {
        viewModelScope.launch {
            state.sheetSelection.collect { selection ->
                if (selection.isNotEmpty()) state.setChosenSheet(0)
            }
        }
    }

    override fun onBack() {
        navCallbacks.onBack()
    }

    override fun onGameChange(index: Int) {
        if (index !in state.gameSelection.value.indices) {
            assert(true) { "Index of a game requested is outside the range" }
            return
        }

        state.setChosenGame(index)
        val game = state.gameSelection.value[index]
        model.requestSheetList(game)
    }

    override fun onSheetChange(index: Int) {
        if (index !in state.sheetSelection.value.indices) {
            assert(true) { "Index of a sheet requested is outside the range" }
            return
        }

        state.setChosenSheet(index)
    }

    override fun onSizeUp() {
        state.incrementSideCount(Direction.UP)
    }

    override fun onSizeDown() {
        state.incrementSideCount(Direction.DOWN)
    }

    override fun onDone() {
        if(state.chosenGame.value == null || state.chosenSheet.value == null) {
            assert(true) { "Not all options are set" }
            return
        }

        state.chosenGame.value?.let { gameIndex ->
            state.chosenSheet.value?.let { sheetIndex ->
                navCallbacks.onStartGame(
                    state.gameSelection.value[gameIndex],
                    state.sheetSelection.value[sheetIndex],
                    state.boardSideCount.value,
                )
            }
        }
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

class GameSetupState(): IGameSetupState {
    private val minSideCount = 2
    private val defaultSideCount = 5

    override val gameSelection: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    override val chosenGame: MutableStateFlow<Int?> = MutableStateFlow(null)
    override val sheetSelection: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    override val chosenSheet: MutableStateFlow<Int?> = MutableStateFlow(null)
    override val boardSideCount: MutableStateFlow<Int> = MutableStateFlow(defaultSideCount)
    override val snackBarMessage: MutableSharedFlow<String> = MutableSharedFlow()

    override fun didLoadGames(games: List<String>) {
        gameSelection.value = games
    }

    override fun didLoadSheets(sheets: List<String>) {
        sheetSelection.value = sheets
    }

    override suspend fun didServerCallFailed(message: String) {
        snackBarMessage.emit(message)
    }

    override fun setChosenGame(index: Int) {
        require(index in gameSelection.value.indices) {
            throw IllegalArgumentException("Index outside of range")
        }

        chosenGame.value = index
    }

    override fun setChosenSheet(index: Int) {
        require(index in sheetSelection.value.indices) {
            throw IllegalArgumentException("Index outside of range")
        }

        chosenSheet.value = index
    }

    override fun incrementSideCount(direction: Direction) {
        boardSideCount.value = (boardSideCount.value + direction.sign).coerceAtLeast(minSideCount)
    }
}
