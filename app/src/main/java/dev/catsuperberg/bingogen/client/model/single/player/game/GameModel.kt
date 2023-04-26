package dev.catsuperberg.bingogen.client.model.single.player.game

import dev.catsuperberg.bingogen.client.model.interfaces.IGameModel
import dev.catsuperberg.bingogen.client.view.model.common.game.IGameModelReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameModel(private val receiver: IGameModelReceiver): IGameModel {
    override fun requestBoard() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(500) // Api call
            receiver.didLoadBoard(listOf(listOf("hello", "hey"), listOf("thingamabob", "what")))
        }
    }
}
