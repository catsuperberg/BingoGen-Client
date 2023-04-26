package dev.catsuperberg.bingogen.client.model.multiplayer.gamesetup

import dev.catsuperberg.bingogen.client.model.interfaces.IGameSetupModel
import dev.catsuperberg.bingogen.client.view.model.common.gamesetup.IGameSetupModelReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameSetupModel(private val receiver: IGameSetupModelReceiver): IGameSetupModel {
    override fun requestGameList() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(500) // Api call
            receiver.didLoadGames(listOf("multiplayer", "still multiplayer"))
        }
    }

    override fun requestSheetList() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(500) // Api call
            receiver.didLoadSheets(listOf("definitely not single player", "never single"))
        }
    }
}
