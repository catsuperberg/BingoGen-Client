package dev.catsuperberg.bingogen.client.api

import kotlinx.serialization.Serializable

@Serializable
data class TaskDTO(
    val dbid: Long,
    val shortText: String,
    val description: String,
    val timeToKeepMS: Long?,
    val fromStart: Boolean
)
