package dev.catsuperberg.bingogen.client.model.multiplayer.game

import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameModelReceiver

class GameModel(private val receiver: IGameModelReceiver): IGameModel {
    override fun requestDetailsUpdates(tileIndex: Int) {
        TODO("Not yet implemented")
    }

    override fun stopDetailsUpdates() {
        TODO("Not yet implemented")
    }

    override fun toggleTaskDone(tileIndex: Int, state: Boolean?) {
        TODO("Not yet implemented")
    }

    override fun toggleTaskTimer(tileIndex: Int, state: Boolean?) {
        TODO("Not yet implemented")
    }

    override fun toggleTaskKeptFromStart(taskIndex: Int, state: Boolean?) {
        TODO("Not yet implemented")
    }

    override fun restartTaskTimer(tileIndex: Int) {
        TODO("Not yet implemented")
    }
}
