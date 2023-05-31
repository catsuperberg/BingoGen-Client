package dev.catsuperberg.bingogen.client.view.model.common.game

import android.os.Parcelable
import androidx.compose.runtime.State
import kotlinx.parcelize.Parcelize

interface IGameFields {
    val selectedGame: State<String>
    val selectedSheet: State<String>
    val board: State<List<String>>
}

interface IGameRequests {
    fun requestBack()
}

interface IGameViewModel: IGameFields, IGameRequests {
    data class NavCallbacks(
        val onBack: () -> Unit,
    )
    @Parcelize
    data class Selection(
        val game: String,
        val sheet: String,
        val sideCount: Int,
    ) : Parcelable
}

interface IGameModelReceiver {
    fun didLoadBoard(board: List<List<String>>)
}

interface IGameState: IGameFields, IGameModelReceiver {
    fun setGame(game: String)
    fun setSheet(sheet: String)
}
