package dev.catsuperberg.bingogen.client.api

import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Task

interface IGridMapper {
    fun map(dto: GridDTO): Grid<Task>
    fun map(grid: Grid<Task>): GridDTO
}
