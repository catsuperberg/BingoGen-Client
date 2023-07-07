package dev.catsuperberg.bingogen.client.api

import dev.catsuperberg.bingogen.client.common.Task
import dev.catsuperberg.bingogen.client.common.TaskState
import dev.catsuperberg.bingogen.client.common.TaskStatus
import org.joda.time.Duration

class TaskMapper : ITaskMapper{
    override fun map(dto: TaskDTO): Task {
        return Task(
            dto.dbid,
            dto.shortText,
            dto.description,
            TaskState(
                timeToKeep = dto.timeToKeepMS?.let { Duration.millis(it) },
                keptFromStart = if (dto.fromStart) false else null,
                status = TaskStatus.ACTIVE,
            )
        )
    }

    override fun map(task: Task): TaskDTO {
        return TaskDTO(
            task.dbid,
            task.shortText,
            task.description,
            task.state.timeToKeep?.millis,
            task.state.keptFromStart != null,
        )
    }
}
