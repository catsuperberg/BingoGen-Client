package dev.catsuperberg.bingogen.client.service.test.data

import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Task
import org.joda.time.Duration
import java.util.Collections

object TestServerResponses {
    val unsuccessfulStatusCodes = listOf(
        400, // Bad Request
        401, // Unauthorized
        403, // Forbidden
        404, // Not Found
        405, // Method Not Allowed
        406, // Not Acceptable
        409, // Conflict
        410, // Gone
        422, // Unprocessable Entity
        429, // Too Many Requests
        500, // Internal Server Error
        501, // Not Implemented
        502, // Bad Gateway
        503, // Service Unavailable
        504  // Gateway Timeout
    )

    object Game {
        val expected = listOf("Test Game", "Cities Skylines")
        const val body = "[\"Test Game\",\"Cities Skylines\"]"
    }

    object Sheets {
        val expected = listOf("Basic", "Hard")
        const val body = "[\"Basic\",\"Hard\"]"
    }

    object Board {
        private const val sideCount = 5
        private val taskPresets = setOf(
                Task(1, "test", "test", Duration.millis(1), true),
                Task(2, "test", "test", Duration.millis(1), false),
                Task(3, "test", "test", null, false),
                Task(4, "test", "", null, false),
                Task(0, "", "", null, false),
            )

        private val boardTasks by lazy {
            val taskList = taskPresets.toMutableList()
            val nextTask = { taskList.first().also { Collections.rotate(taskList, -1) } }
            ArrayDeque(
                List(sideCount * sideCount) { index ->
                    nextTask().copy(dbid = index.toLong())
                }
            )
        }

        private fun Task.asText() =
            """
            "dbid": $dbid,
            "shortText": "$shortText",
            "description": "$description",
            "timeToKeepMS": ${timeToKeep?.millis.toString()},
            "fromStart": ${fromStart.toString()}
            """.trimIndent()

        val expected = Grid(boardTasks.toList().chunked(sideCount))
        val body =
            """
            |{
            |    "rows": [
            |${rowsOfTasks(sideCount).prependIndent("        ")}
            |    ]
            |}
            """.trimMargin("|")

        private fun rowOfTasks(count: Int) = List(count) {
            """
            |{
            |${boardTasks.removeFirst().asText().prependIndent("    ")}
            |}
            """.trimMargin("|")
        }.joinToString(",\n")

        private fun rowsOfTasks(count: Int) = List(count) {
            """
            |[
            |${rowOfTasks(count).prependIndent("   ")}
            |]
            """.trimMargin("|")
        }.joinToString(",\n")
    }
}
