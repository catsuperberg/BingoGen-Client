package dev.catsuperberg.bingogen.client.service

import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Grid.Companion.toGrid
import dev.catsuperberg.bingogen.client.common.Task
import dev.catsuperberg.bingogen.client.common.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.Interval

class TaskBoard(
    private val initialTasks: Grid<Task>,
    private val scope: CoroutineScope = CoroutineScope(Job() + Dispatchers.Default),
    private val timeGetter: () -> Instant = { Instant.now() }
) : ITaskBoard {
    companion object {
        val gracePeriod: Duration = Duration.standardSeconds(20)
        const val timerUpdateInterval: Long = 150L
    }

    private val _tasks = MutableStateFlow(initialTasks)
    override val tasks: StateFlow<Grid<Task>> = _tasks
    private val _hasBingo = MutableStateFlow(hasBingo(initialTasks))
    override val hasBingo: StateFlow<Boolean> = _hasBingo
    private val _hasKeptBingo = MutableStateFlow(hasKeptBingo(initialTasks))
    override val hasKeptBingo: StateFlow<Boolean> = _hasKeptBingo
    private val timerInitiationInstants: MutableMap<Int, Instant> = mutableMapOf()
    private var inactive = false

    private val hasBingoJob: Job = scope.launch { tasks.map(::hasBingo).collect(_hasBingo::emit) }
    private val keptBingoJob: Job = scope.launch { tasks.map(::hasKeptBingo).collect(_hasKeptBingo::emit) }
    private val timerTickJob: Job = scope.launch { timersTick() }
    private val gracePeriodJob: Job = scope.launch { failUnkeptTasks(gracePeriod) }
    private val inGracePeriod: Boolean
        get() = gracePeriodJob.isCompleted.not()

    private suspend fun timersTick() {
        while(true) {
            delay(timerUpdateInterval)
            val currentTime = now()
            val newTimesToKeep = timerInitiationInstants
                .mapValues { Interval(it.value, currentTime).toDuration() }
                .mapValues { initialTasks[it.key].state.timeToKeep?.let { initial -> initial - it.value } }
                .mapValues { it.value?.let { duration -> if (duration > Duration.ZERO) duration else null } }

            _tasks.value = _tasks.value.mapIndexed { index, task ->
                if (index !in newTimesToKeep) task
                else {
                    val timerValue = newTimesToKeep[index]
                    task.copy(state = task.state.copy(timeToKeep = timerValue, status = statusFromTimer(timerValue, task)))
                }
            }.toGrid()
            newTimesToKeep.filter { it.value == null }
                .forEach { timerInitiationInstants.remove(it.key) }
        }
    }

    private fun statusFromTimer(timerValue: Duration?, task: Task) =
        if (timerValue == null) if (task.state.keptFromStart == null) TaskStatus.DONE else TaskStatus.KEPT
        else if (task.state.keptFromStart != null) TaskStatus.KEPT_COUNTDOWN else TaskStatus.COUNTDOWN

    private fun hasBingo(grid: Grid<Task>): Boolean {
        val linesToCheck = grid.rows + grid.columns
        return linesToCheck.any { row -> row.all { task -> task.state.status == TaskStatus.DONE } }
    }

    private fun hasKeptBingo(grid: Grid<Task>): Boolean {
        val linesToCheck = grid.rows + grid.columns
        return linesToCheck.any(::lineHasKeptBingo)
    }

    private suspend fun failUnkeptTasks(postponeFor: Duration) {
        delay(postponeFor.millis)
        _tasks.value = _tasks.value.map { task ->
            task.state.keptFromStart?.let { kept ->
                if (kept.not()) task.updateStatus(TaskStatus.FAILED)
                else task
            } ?: task
        }.toGrid()
    }


    override fun toggleDone(taskIndex: Int, state: Boolean?) {
        if (inactive) return
        if (_tasks.value[taskIndex].state.status == TaskStatus.FAILED) return

        _tasks.value = _tasks.value.mapIndexed { index, task ->
            if(index == taskIndex) {
                val current = task.state.status.toBoolean()
                val stateToSet = state ?: current.not()
                if (current && stateToSet.not())
                    initialTasks[taskIndex]
                else
                    task.updateStatus(stateToSet.toStatus(task.state.keptFromStart != null))
            } else { task }
        }.toGrid()
    }

    override fun toggleKeptFromStart(taskIndex: Int, state: Boolean?) {
        if (inactive) return
        if (tasks.value[taskIndex].state.keptFromStart == null) return
        val resultState = updateKeptFromStart(taskIndex, state)
        if (tasks.value[taskIndex].state.timeToKeep != null)
            updateTaskTimer(taskIndex, resultState)
    }

    override fun toggleTaskTimer(taskIndex: Int, state: Boolean?) {
        if (inactive) return
        if (initialTasks[taskIndex].state.timeToKeep == null) return
        val resultState = updateTaskTimer(taskIndex, state)
        if (tasks.value[taskIndex].state.keptFromStart != null)
            updateKeptFromStart(taskIndex, resultState)
    }

    private fun updateKeptFromStart(taskIndex: Int, state: Boolean?) : Boolean {
        if (_tasks.value[taskIndex].state.status == TaskStatus.FAILED) return false
        var finalState = false

        _tasks.value[taskIndex].state.keptFromStart?.also { currentKept ->
            _tasks.value = _tasks.value.mapIndexed { index, task ->
                if (index == taskIndex) {
                    finalState = state ?: currentKept.not()
                    if (finalState) task.copy(state = task.state.copy(keptFromStart = true, status = TaskStatus.KEPT))
                    else task.copy(
                        state = task.state.copy(
                            timeToKeep = if (inGracePeriod) task.state.timeToKeep else initialTasks[index].state.timeToKeep,
                            keptFromStart = false,
                            status = if (inGracePeriod) TaskStatus.UNKEPT else TaskStatus.FAILED,
                        )
                    )
                } else {
                    task
                }
            }.toGrid()
        }
        return finalState
    }

    private fun updateTaskTimer(taskIndex: Int, state: Boolean?) : Boolean {
        val finalState = state ?: timerInitiationInstants.containsKey(taskIndex).not()
        if (finalState) timerInitiationInstants.computeIfAbsent(taskIndex) {
            _tasks.value = _tasks.value.mapIndexed { index, task ->
                if (index != taskIndex) task
                else task.copy(state = task.state.copy(status = if (task.state.keptFromStart != null) TaskStatus.KEPT_COUNTDOWN else TaskStatus.COUNTDOWN))
            }.toGrid()
            now()
        }
        else stopAndResetTimer(taskIndex)
        return finalState
    }


    private fun stopAndResetTimer(taskIndex: Int) {
        if (_tasks.value[taskIndex].state.status in TaskStatus.WithActiveTimer) {
            _tasks.value = _tasks.value.mapIndexed { index, task ->
                if (index != taskIndex) task
                else task.copy(state = task.state.copy(
                    status = if (task.state.keptFromStart != null) TaskStatus.UNKEPT else TaskStatus.UNDONE,
                    timeToKeep = initialTasks[index].state.timeToKeep,
                ))
            }.toGrid()
        }
        timerInitiationInstants.remove(taskIndex)
    }

    override fun resetTaskTimer(taskIndex: Int) {
        if (inactive) return
        _tasks.value = _tasks.value.mapIndexed { index, task ->
            if (index == taskIndex)
                task.copy(state = task.state.copy(timeToKeep = initialTasks[taskIndex].state.timeToKeep))
            else
                task
        }.toGrid()
        timerInitiationInstants[taskIndex] = now()
    }

    private fun now(): Instant = timeGetter()

    override fun markKeptDoneIfResultsInBingo() {
        if (hasKeptBingo.value.not()) return

        val rowIndicesWithKeptBingo = indicesWithKeptBingoOnly(tasks.value.rows)
        val columnIndicesWithKeptBingo = indicesWithKeptBingoOnly(tasks.value.columns)

        val rowsMarked = markKeptRowsAsDone(tasks.value.rows, rowIndicesWithKeptBingo)
        val allMarkedAsColumns = markKeptRowsAsDone(Grid.fromRows(rowsMarked).columns, columnIndicesWithKeptBingo)
        val correctedRotation = Grid.fromRows(Grid.fromRows(allMarkedAsColumns).columns)

        _tasks.value = correctedRotation
    }

    private fun markKeptRowsAsDone(
        lines: List<List<Task>>,
        keptRows: List<Int>
    ) = lines.mapIndexed { index, tasks ->
        if (index !in keptRows) tasks
        else tasks.map {
            if (it.state.status == TaskStatus.DONE) it
            else it.copy(state = it.state.copy(timeToKeep = null, keptFromStart = true, status = TaskStatus.DONE))
        }
    }

    private fun indicesWithKeptBingoOnly(lines: List<List<Task>>) =
        lines.mapIndexedNotNull { index, line ->
            if (lineHasKeptBingo(line)) index
            else null
        }

    private fun lineHasKeptBingo(line: List<Task>): Boolean {
        val hasAnyBingo = line.all { it.state.status == TaskStatus.DONE || it.state.keptFromStart == true }
        val hasKept = line.any { it.state.keptFromStart == true }
        val notStraightBingo = line.all { it.state.status == TaskStatus.DONE }.not()
        return hasAnyBingo && hasKept && notStraightBingo
    }

    override fun cancelScopeJobs() {
        timerTickJob.cancel()
        timerInitiationInstants.clear()
        gracePeriodJob.cancel()
        hasBingoJob.cancel()
        keptBingoJob.cancel()
    }

    override fun stopInteractions() {
        inactive = true
    }

    private fun Task.updateStatus(newStatus: TaskStatus) =
        if (this.state.status != TaskStatus.FAILED) this.copy(state = this.state.copy(status = newStatus))
        else this

    private fun Boolean.toStatus(hasKept: Boolean) = if(this) TaskStatus.DONE else if (hasKept) TaskStatus.UNKEPT else TaskStatus.UNDONE
    private fun TaskStatus.toBoolean() = when(this) {
        TaskStatus.DONE, TaskStatus.KEPT -> true
        TaskStatus.UNDONE, TaskStatus.UNKEPT, TaskStatus.COUNTDOWN -> false
        else -> throw IllegalArgumentException("Can only convert ${TaskStatus.DONE.name} and ${TaskStatus.UNDONE.name} to Boolean. But ${this.name} conversion was attempted.")
    }
}
