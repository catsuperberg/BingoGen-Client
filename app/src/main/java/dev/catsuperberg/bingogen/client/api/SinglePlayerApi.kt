package dev.catsuperberg.bingogen.client.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SinglePlayerApi {
    @GET("game")
    fun getGames(): Call<List<String>>

    @GET("game/{game_name}")
    fun getSheets(@Path("game_name") name: String): Call<List<String>>

    @GET("board/")
    fun getBoard(
        @Query("side_count") sideCount: Int,
        @Query("game_name") name: String,
        @Query("task_sheet") sheet: String,
    ): Call<GridDTO>
}
