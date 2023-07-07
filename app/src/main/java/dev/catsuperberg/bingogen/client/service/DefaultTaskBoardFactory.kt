package dev.catsuperberg.bingogen.client.service

import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Task
import kotlinx.coroutines.CoroutineScope

class DefaultTaskBoardFactory : ITaskBoardFactory {
    override fun create(tasks: Grid<Task>, scope: CoroutineScope?) =
        scope?.let { TaskBoard(tasks, it) } ?: TaskBoard(tasks)
}
