package dev.catsuperberg.bingogen.client.service

import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Task

class TaskApiException(message: String) : Exception(message)

interface ITaskRetriever {
    fun getGames() : List<String>
    fun getSheets(game: String) : List<String>
    fun getBoard(sideCount: Int, game: String, sheet: String) : Grid<Task>
}
