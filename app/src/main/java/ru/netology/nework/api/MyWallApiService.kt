package ru.netology.nework.api

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.dto.Post

interface MyWallApiService {

    @POST("my/wall/{id}/likes")
    suspend fun likePost(@Path("id") id: Long): Response<Post>

    @DELETE("my/wall/{id}/likes")
    suspend fun unlikePost(@Path("id") id: Long): Response<Post>

    @GET("my/wall")
    suspend fun readMyWall(): Response<List<Post>>

    @GET("my/wall/{id}/newer")
    suspend fun getNewerMyWall(@Path("id") postId: Long): Response<List<Post>>

    @GET("my/wall/{id}/before")
    suspend fun getBeforeMyWall(@Path("id") postId: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("my/wall/{id}/after")
    suspend fun getAfterMyWall(@Path("id") postId: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("my/wall/{id}")
    suspend fun getPost(@Path("id") postId: Long): Response<Post>

    @GET("my/wall/latest")
    suspend fun getLatestMyWall(@Query("count") count: Int): Response<List<Post>>






}