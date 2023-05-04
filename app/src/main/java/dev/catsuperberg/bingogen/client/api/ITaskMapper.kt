package dev.catsuperberg.bingogen.client.api

import dev.catsuperberg.bingogen.client.common.Task

interface ITaskMapper {
    fun map(dto: TaskDTO): Task
    fun map(task: Task): TaskDTO
}
