package dev.catsuperberg.bingogen.client.model.single.player.gamesetup

import dev.catsuperberg.bingogen.client.model.common.BaseModel
import dev.catsuperberg.bingogen.client.model.interfaces.IGameSetupModel
import dev.catsuperberg.bingogen.client.service.ITaskRetriever
import dev.catsuperberg.bingogen.client.service.TaskApiException
import dev.catsuperberg.bingogen.client.view.model.common.gamesetup.IGameSetupModelReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

class GameSetupModel(
    private val receiver: IGameSetupModelReceiver,
    private val retriever: ITaskRetriever,
    modelScope: CoroutineScope = defaultScope,
): IGameSetupModel, BaseModel(modelScope) {
    private val loadGames = {
        val games = retriever.getGames()
        receiver.didLoadGames(games)
    }

    private val loadSheets = { gameName: String ->
        val sheets = retriever.getSheets(gameName)
        receiver.didLoadSheets(sheets)
    }

    override fun requestGameList() {
        scope.launch {
            performAndCatchApiCallErrors(loadGames)
        }
    }

    override fun requestSheetList(gameName: String) {
        scope.launch {
            val action = { loadSheets(gameName) }
            performAndCatchApiCallErrors(action)
        }
    }

    private suspend fun performAndCatchApiCallErrors(actionWithApiCall: () -> Unit) {
        try {
            actionWithApiCall()
        } catch (e: TaskApiException) {
            receiver.didServerCallFailed("${e.message}")
        } catch (e: SocketTimeoutException) {
            receiver.didServerCallFailed("${e.message}")
        }
    }
}
