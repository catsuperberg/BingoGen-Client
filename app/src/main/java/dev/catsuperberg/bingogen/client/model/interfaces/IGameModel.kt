package dev.catsuperberg.bingogen.client.model.interfaces

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

interface IGameModel {
    fun requestDetailsUpdates(tileIndex: Int)
    fun stopDetailsUpdates()
    fun toggleTaskDone(tileIndex: Int, state: Boolean? = null)
    fun toggleTaskTimer(tileIndex: Int, state: Boolean? = null)
    fun toggleTaskKeptFromStart(taskIndex: Int, state: Boolean? = null)
    fun restartTaskTimer(tileIndex: Int)

    @Parcelize
    data class Selection(
        val game: String,
        val sheet: String,
        val sideCount: Int,
    ) : Parcelable
}
