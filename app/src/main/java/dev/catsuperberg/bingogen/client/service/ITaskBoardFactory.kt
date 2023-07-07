package dev.catsuperberg.bingogen.client.service

import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Task
import kotlinx.coroutines.CoroutineScope

interface ITaskBoardFactory {
    fun create(tasks: Grid<Task>, scope: CoroutineScope? = null) : ITaskBoard
}
