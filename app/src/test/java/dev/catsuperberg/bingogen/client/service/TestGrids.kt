package dev.catsuperberg.bingogen.client.service

import dev.catsuperberg.bingogen.client.common.DefaultTaskGrid
import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Grid.Companion.toGrid
import dev.catsuperberg.bingogen.client.common.Task
import dev.catsuperberg.bingogen.client.common.TaskStatus
import kotlin.math.pow

object TestGrids {
    const val taskIdWithTimeToKeep = DefaultTaskGrid.taskIdWithTimeToKeep
    const val taskIdWithTimeToKeepAndKept = DefaultTaskGrid.taskIdWithTimeToKeepAndKept
    const val taskIdWithUnkeptFailed = DefaultTaskGrid.taskIdWithUnkeptFailed
    const val taskIdWithTimeToKeepWithoutKeepFromStart = DefaultTaskGrid.taskIdWithTimeToKeepWithoutKeepFromStart

    val defaultGrid = DefaultTaskGrid.grid

    private val boolCombinations = boolCombinations(defaultGrid.rows.size)
        .filter { sequence -> sequence.any { it } }

    private val rowBingoGrids = boolCombinations.map { combination ->
        Grid.fromRows(defaultGrid.rows.mapBoolCombination(combination))
    }
    private val columnBingoGrids = boolCombinations.map { combination ->
        Grid.fromRows(Grid.fromRows(defaultGrid.columns.mapBoolCombination(combination)).columns)
    }


    val gridWithFailedKept = defaultGrid.map { task ->
        if (task.state.keptFromStart != null) task.copy(state = task.state.copy(status = TaskStatus.FAILED)) else task
    }.toGrid()

    val gridWithFirstDone = defaultGrid.mapIndexed { index, task ->
        if (index == 0) task.copy(state = task.state.copy(status = TaskStatus.DONE)) else task
    }.toGrid()

    val gridWithFirstTwoDone = defaultGrid.mapIndexed { index, task ->
        if (index in 0..1) task.copy(state = task.state.copy(status = TaskStatus.DONE)) else task
    }.toGrid()

    val failedTasksAt = gridWithFailedKept.mapIndexedNotNull { index, task ->
        if (task.state.status == TaskStatus.FAILED) index else null
    }

    val nullKeptTasksAt = gridWithFailedKept.mapIndexedNotNull { index, task ->
        if (task.state.keptFromStart == null) index else null
    }

    val gridWithFirstKept = defaultGrid.mapIndexed { index, task ->
        if (index == 0) task.copy(state = task.state.copy(keptFromStart = true)) else task
    }.toGrid()

    val gridWithFirstTwoKept = defaultGrid.mapIndexed { index, task ->
        if (index in 0..1) task.copy(state = task.state.copy(keptFromStart = true)) else task
    }.toGrid()

    val gridWithFirstTwoKeptAndOthersFailed = gridWithFailedKept.mapIndexed { index, task ->
        if (index in 0..1) task.markAsKept() else task
    }.toGrid()

    val bingoGrids: Set<Grid<Task>> = run {
        val combinationBingoGrids = rowBingoGrids.flatMap { bingoOnRows ->
            columnBingoGrids.map { bingoOnColumns ->
                bingoOnRows.mapIndexed { index, task ->
                    if (bingoOnColumns[index].state.status == TaskStatus.DONE)
                        bingoOnColumns[index]
                    else
                        task
                }.toGrid()
            }
        }

        (rowBingoGrids + columnBingoGrids + combinationBingoGrids).toSet()
    }

    val keptBingoGrids: Set<Grid<Task>> = run {
        val indexedWithBingoRows = rowBingoGrids.map { grid ->
            val bingoRowsAtIndexes: List<Int> = grid.rows.mapIndexedNotNull { index, line ->
                if (line.all { it.state.status == TaskStatus.DONE } ) index else null
            }
            val gridWithBingosAt: Pair<Grid<Task>, List<Int>> = grid to bingoRowsAtIndexes
            gridWithBingosAt
        }

        val boolPatterns = boolCombinations.map { combination ->
            combination.mapIndexedNotNull { index, state -> if (state) index else null }
        }

        val gridsWithSingleRowBingoIndexed = indexedWithBingoRows.flatMap { grid ->
            grid.second.flatMap { rowWithBingo ->
                boolPatterns.map { pattern ->
                    val newRows = grid.first.rows.mapIndexed { rowIndex, row ->
                        if (rowIndex != rowWithBingo) row
                        else row.mapIndexed { taskIndex, task ->
                            if (taskIndex !in pattern) task
                            else task.markAsKept()
                        }
                    }
                    Grid.fromRows(newRows) to rowWithBingo
                }
            }
        }.toSet()

        val gridsWithAllRowBingoVariants = gridsWithSingleRowBingoIndexed.map { gridWithBingoIndex ->
            variantsOfBingoRowGrid(gridWithBingoIndex.first, gridWithBingoIndex.second)
        }.flatten().toSet()

        val columnsWithAllVariations = gridsWithAllRowBingoVariants.map { grid ->
            Grid.fromRows(grid.columns)
        }

        (gridsWithAllRowBingoVariants + columnsWithAllVariations).toSet()
    }

    private fun variantsOfBingoRowGrid(
        variantWithKeptBingo: Grid<Task>,
        rowIndexWithBingo: Int
    ): List<Grid<Task>> {
        val ignoreStartIndex = variantWithKeptBingo.sideCount * rowIndexWithBingo
        val indicesToIgnore =
            ignoreStartIndex until ignoreStartIndex + variantWithKeptBingo.sideCount
        val indicesToToggle = variantWithKeptBingo.mapIndexedNotNull { index, task ->
            if (index in indicesToIgnore || task.state.status != TaskStatus.DONE) null
            else index
        }
        return if (indicesToToggle.isEmpty())
            listOf(variantWithKeptBingo)
        else {
            val patterns = boolCombinations(indicesToToggle.size)
            val changePatterns = patterns.map { boolPattern ->
                boolPattern.mapIndexedNotNull { index, state ->
                    if (state.not()) null
                    else indicesToToggle[index]
                }
            }
            val variantsWithKeptInOtherPlaces = changePatterns.map { pattern ->
                variantWithKeptBingo.mapIndexed { index, task ->
                    if (index !in pattern) task
                    else task.markAsKept()
                }.toGrid()
            }
            variantsWithKeptInOtherPlaces
        }
    }

    private fun Task.markAsKept() = this.copy(
        state = this.state.copy(
            timeToKeep = null,
            keptFromStart = true,
            status = TaskStatus.ACTIVE
        )
    )

    private fun boolCombinations(size: Int): Set<List<Boolean>> {
        val possibleValues = setOf(true, false)
        val resultSize = possibleValues.count().toDouble().pow(size).toInt()
        var sequences = possibleValues.asSequence().map { sequenceOf(it) }
        while (sequences.count() < resultSize) {
            sequences = lazyContinuousCartesianProduct(sequences, possibleValues)
        }

        return sequences.map { it.toList() }.toSet()
    }

    private fun List<List<Task>>.mapBoolCombination(combination: List<Boolean>) =
        this.mapIndexed { index, taskLine ->
            if (combination[index]) taskLine.setLineAsDone() else taskLine
        }

    private fun List<Task>.setLineAsDone() =
        this.map { it.copy(state = it.state.copy(status = TaskStatus.DONE)) }

    private fun <A> lazyContinuousCartesianProduct(
        partialSequences: Sequence<Sequence<A>>,
        partVariants: Iterable<A>,
    ): Sequence<Sequence<A>> =
        sequence {
            partialSequences.forEach { sequence ->
                partVariants.forEach { addition ->
                    yield(sequence + addition)
                }
            }
        }
}
