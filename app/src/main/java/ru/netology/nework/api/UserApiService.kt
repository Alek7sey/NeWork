package ru.netology.nework.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.dto.Token
import ru.netology.nework.dto.User


interface UserApiService {

    @Multipart
    @POST("users/registration")
    suspend fun regUser(
        @Part("login") login: RequestBody,
        @Part("pass") password: RequestBody,
        @Part("name") name: RequestBody,
        @Part media: MultipartBody.Part
    ): Response<Token>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun registerUserWithoutAvatar(
        @Part("login") login: RequestBody,
        @Part("pass") password: RequestBody,
        @Part("name") name: RequestBody
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