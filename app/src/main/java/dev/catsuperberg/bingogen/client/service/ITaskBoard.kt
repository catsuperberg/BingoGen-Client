package dev.catsuperberg.bingogen.client.service

import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Task
import kotlinx.coroutines.flow.StateFlow

interface ITaskBoard {
    val tasks: StateFlow<Grid<Task>>
    val hasKeptBingo: StateFlow<Boolean>
    val hasBingo: StateFlow<Boolean>
    fun toggleDone(taskIndex: Int, state: Boolean? = null)
    fun toggleKeptFromStart(taskIndex: Int, state: Boolean? = null)
    fun toggleTaskTimer(taskIndex: Int, state: Boolean? = null)
    fun resetTaskTimer(taskIndex: Int)
    fun markKeptDoneIfResultsInBingo()
    fun cancelScopeJobs()
    fun stopInteractions()
}
