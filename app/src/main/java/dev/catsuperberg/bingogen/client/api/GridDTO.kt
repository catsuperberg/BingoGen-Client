package dev.catsuperberg.bingogen.client.api

import kotlinx.serialization.Serializable

@Serializable
data class GridDTO(
    val rows: List<List<TaskDTO>>
)
