package ru.netology.nework.api

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.dto.Post

interface UserWallApiService {

    @POST("{authorId}/wall/{id}/likes")
    suspend fun likePostUser(@Path("authorId") authorId: Long, @Path("id") id: Long): Response<Post>

    @DELETE("{authorId}/wall/{id}/likes")
    suspend fun unlikePostUser(@Path("authorId") authorId: Long, @Path("id") id: Long): Response<Post>

    @GET("{authorId}/wall")
    suspend fun readUserWall(@Path("authorId") authorId: Long): Response<List<Post>>

    @GET("{authorId}/wall/{id}/newer")
    suspend fun getNewerUserWall(@Path("authorId") authorId: Long, @Path("id") postId: Long): Response<List<Post>>

    @GET("{authorId}/wall/{id}/before")
    suspend fun getBeforeUserWall(@Path("authorId") authorId: Long, @Path("id") postId: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("{authorId}/wall/{id}/after")
    suspend fun getAfterUserWall(@Path("authorId") authorId: Long, @Path("id") postId: Long, @Query("count") count: Int): Response<List<Post>>

    @GET("{authorId}/wall/{id}")
    suspend fun getUserPost(@Path("authorId") authorId: Long, @Path("id") postId: Long): Response<Post>

    @GET("{authorId}/wall/latest")
    suspend fun getLatestUserWall(@Path("authorId") authorId: Long, @Query("count") count: Int): Response<List<Post>>

}