package dev.catsuperberg.bingogen.client.model.single.player.gamesetup

import dev.catsuperberg.bingogen.client.model.common.BaseModel
import dev.catsuperberg.bingogen.client.model.interfaces.IGameSetupModel
import dev.catsuperberg.bingogen.client.service.ITaskRetriever
import dev.catsuperberg.bingogen.client.service.TaskApiException
import dev.catsuperberg.bingogen.client.view.model.common.gamesetup.IGameSetupModelReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class GameSetupModel(
    private val receiver: IGameSetupModelReceiver,
    private val retriever: ITaskRetriever,
    modelScope: CoroutineScope = defaultScope,
): IGameSetupModel, BaseModel(modelScope) {
    override fun requestGameList() {
        scope.launch {
            try {
                val games = retriever.getGames()
                receiver.didLoadGames(games)
            } catch (e: TaskApiException) {
                receiver.didServerCallFailed("${e.message}")
            }
        }
    }

    override fun requestSheetList(gameName: String) {
        scope.launch {
            try {
                val sheets = retriever.getSheets(gameName)
                receiver.didLoadSheets(sheets)
            } catch (e: TaskApiException) {
                receiver.didServerCallFailed("${e.message}")
            }
        }
    }
}
