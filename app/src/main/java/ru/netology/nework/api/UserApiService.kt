package ru.netology.nework.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.dto.Token
import ru.netology.nework.dto.User


interface UserApiService {

    @Multipart
    @POST("users/registration")
    suspend fun regUser(
        @Field("login") login: String,
        @Field("pass") password: String,
        @Field("name") name: String,
        @Part media: MultipartBody.Part
    ): Response<Token>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun registerUserWithoutAvatar(
        @Field("login") login: String,
        @Field("pass") password: String,
        @Field("name") name: String,
    ): Response<Token>

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun authUser(
        @Field("login") login: String,
        @Field("pass") password: String
    ): Response<Token>

    @GET("users")
    suspend fun readAllUsers(): Response<List<User>>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Long): Response<User>

}