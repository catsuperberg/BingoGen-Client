package dev.catsuperberg.bingogen.client.api

import dev.catsuperberg.bingogen.client.common.Task
import org.joda.time.Duration

class TaskMapper : ITaskMapper{
    override fun map(dto: TaskDTO): Task {
        return Task(
            dto.dbid,
            dto.shortText,
            dto.description,
            dto.timeToKeepMS?.let { Duration.millis(it) },
            dto.fromStart,
        )
    }

    override fun map(task: Task): TaskDTO {
        return TaskDTO(
            task.dbid,
            task.shortText,
            task.description,
            task.timeToKeep?.millis,
            task.fromStart,
        )
    }
}
