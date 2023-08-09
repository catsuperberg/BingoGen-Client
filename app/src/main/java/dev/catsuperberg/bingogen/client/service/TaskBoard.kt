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

class TaskBoard(
    private val initialTasks: Grid<Task>,
    private val scope: CoroutineScope = CoroutineScope(Job() + Dispatchers.Default),
) : ITaskBoard {
    companion object {
        val gracePeriod: Duration = Duration.standardSeconds(20)
    }

    private val _tasks = MutableStateFlow(initialTasks)
    override val tasks: StateFlow<Grid<Task>> = _tasks
    private val _hasBingo = MutableStateFlow(hasBingo(initialTasks))
    override val hasBingo: StateFlow<Boolean> = _hasBingo
    private val _hasKeptBingo = MutableStateFlow(hasKeptBingo(initialTasks))
    override val hasKeptBingo: StateFlow<Boolean> = _hasKeptBingo

    private val hasBingoJob: Job = scope.launch { tasks.map(::hasBingo).collect(_hasBingo::emit) }
    private val keptBingoJob: Job = scope.launch { tasks.map(::hasKeptBingo).collect(_hasKeptBingo::emit) }

    private val timers: MutableMap<Int, Job> = mutableMapOf()
    private val gracePeriodJob: Job = scope.launch { failUnkeptTasks(gracePeriod) }
    private val inGracePeriod: Boolean
        get() = gracePeriodJob.isCompleted.not()

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
        updateKeptFromStart(taskIndex, state)
        if(tasks.value[taskIndex].state.timeToKeep != null)
            updateTaskTimer(taskIndex, state)
    }

    override fun toggleTaskTimer(taskIndex: Int, state: Boolean?) {
        updateTaskTimer(taskIndex, state)
        if(tasks.value[taskIndex].state.keptFromStart != null)
            updateKeptFromStart(taskIndex, state)
    }

    private fun updateKeptFromStart(taskIndex: Int, state: Boolean?) {
        if (_tasks.value[taskIndex].state.status == TaskStatus.FAILED) return

        _tasks.value[taskIndex].state.keptFromStart?.also { currentKept ->
            _tasks.value = _tasks.value.mapIndexed { index, task ->
                if (index == taskIndex) {
                    val stateToSet = state ?: currentKept.not()
                    if (stateToSet) task.copy(state = task.state.copy(keptFromStart = true, status = TaskStatus.KEPT))
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
    }

    private fun updateTaskTimer(taskIndex: Int, state: Boolean?) {
        val stateToSet = state ?: timers.containsKey(taskIndex).not()
        if(stateToSet) timers.computeIfAbsent(taskIndex) { scope.launch { timerTick(taskIndex) } }
        else stopAndResetTimer(taskIndex)
    }


    private fun stopAndResetTimer(taskIndex: Int) {
        if (_tasks.value[taskIndex].state.status in listOf(TaskStatus.COUNTDOWN, TaskStatus.KEPT_COUNTDOWN)) {
            _tasks.value = _tasks.value.mapIndexed { index, task ->
                if (index != taskIndex) task
                else task.copy(state = task.state.copy(status = if (task.state.keptFromStart != null) TaskStatus.UNKEPT else TaskStatus.UNDONE))
            }.toGrid()
        }
        timers.computeIfPresent(taskIndex) { _, job ->
            job.cancel()
            resetTaskTimer(taskIndex)
            null
        }
    }

    private suspend fun timerTick(taskIndex: Int) {
        _tasks.value = _tasks.value.mapIndexed { index, task ->
            if (index != taskIndex) task
            else task.copy(state = task.state.copy(status = if (task.state.keptFromStart != null) TaskStatus.KEPT_COUNTDOWN else TaskStatus.COUNTDOWN))
        }.toGrid()

        while(tasks.value[taskIndex].state.timeToKeep != null) {
            delay(1_000)
            _tasks.value = _tasks.value.mapIndexed { index, task ->
                task.state.timeToKeep?.let { duration ->
                    if(index == taskIndex) {
                        val newDuration = duration.minus(1000)
                            .let { if(it <= Duration.ZERO) null else it }
                        if (newDuration != null)
                            task.updateDuration(newDuration)
                        else
                            task.copy(state = task.state.copy(
                                    timeToKeep = null,
                                    status = if (task.state.keptFromStart == null) TaskStatus.DONE else TaskStatus.KEPT
                                )
                            )
                    } else null
                } ?: task
            }.toGrid()
        }
        timers.remove(taskIndex)
    }

    override fun resetTaskTimer(taskIndex: Int) {
        _tasks.value = _tasks.value.mapIndexed { index, task ->
            if (index == taskIndex)
                task.copy(state = task.state.copy(timeToKeep = initialTasks[taskIndex].state.timeToKeep))
            else
                task
        }.toGrid()
    }

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
            else it.copy(state = it.state.copy(null, true,TaskStatus.DONE))
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
        timers.keys.forEach(::stopAndResetTimer)
        timers.clear()
        gracePeriodJob.cancel()
        hasBingoJob.cancel()
        keptBingoJob.cancel()
    }

    private fun Task.updateDuration(newDuration: Duration?) = this
        .copy(state = this.state.copy(timeToKeep = newDuration))

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
