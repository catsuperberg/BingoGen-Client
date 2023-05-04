package dev.catsuperberg.bingogen.client.api

import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Task

class GridMapper(private val taskMapper: ITaskMapper) : IGridMapper {
    override fun map(dto: GridDTO): Grid<Task> {
        val taskRows = dto.rows.map { row -> row.map { taskMapper.map(it) } }
        return Grid(taskRows)
    }

    override fun map(grid: Grid<Task>): GridDTO {
        val dtoRows = grid.rows.map { row -> row.map { taskMapper.map(it) } }
        return GridDTO(dtoRows)
    }
}
