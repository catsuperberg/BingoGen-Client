package dev.catsuperberg.bingogen.client.service

import dev.catsuperberg.bingogen.client.api.IGridMapper
import dev.catsuperberg.bingogen.client.api.SinglePlayerApi
import dev.catsuperberg.bingogen.client.common.Grid
import dev.catsuperberg.bingogen.client.common.Task
import retrofit2.Response
import retrofit2.Retrofit

class TaskRetriever(
    httpClient: Retrofit,
    private val gridMapper: IGridMapper,
) : ITaskRetriever {
    private val api: SinglePlayerApi = httpClient.create(SinglePlayerApi::class.java)

    override fun getGames(): List<String> {
        val response = api.getGames().execute()
        throwOnApiError(response)
        return response.body() ?: listOf()
    }

    override fun getSheets(game: String): List<String> {
        val response = api.getSheets(game).execute()
        throwOnApiError(response)
        return response.body() ?: listOf()
    }

    override fun getBoard(sideCount: Int, game: String, sheet: String): Grid<Task> {
        require(sideCount > 1) { throw IllegalArgumentException("Board side should be at least 2") }
        val response = api.getBoard(sideCount, game, sheet).execute()
        throwOnApiError(response)
        val dto = response.body() ?: return Grid(listOf())
        return gridMapper.map(dto)
    }

    private fun <T> throwOnApiError(response: Response<T>) {
        require(response.isSuccessful) { throw TaskApiException("Api call failed with ${response.code()} ${response.message()}") }
    }
}
