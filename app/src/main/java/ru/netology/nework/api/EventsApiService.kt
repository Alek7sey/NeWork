package ru.netology.nework.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Media

interface EventsApiService {
    @GET("events")
    suspend fun readAllEvents(): Response<List<Event>>

    @POST("events")
    suspend fun saveEvent(@Body event: Event): Response<Event>

    @POST("events/{id}/participants")
    suspend fun becomeParticipants(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/participants")
    suspend fun refuseParticipants(@Path("id") id: Long): Response<Event>

    @POST("events/{id}/likes")
    suspend fun likeEvent(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/likes")
    suspend fun unlikeEvent(@Path("id") id: Long): Response<Event>

    @GET("events/{id}/newer")
    suspend fun getNewerEvents(@Path("id") id: Long): Response<List<Event>>

    @GET("events/{id}/before")
    suspend fun getBeforeEvents(@Path("id") id: Long, @Query("count") count: Int): Response<List<Event>>

    @GET("events/{id}/after")
    suspend fun getAfterEvents(@Path("id") id: Long, @Query("count") count: Int): Response<List<Event>>

    @GET("events/{id}")
    suspend fun getEvent(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") id: Long): Response<Unit>

    @GET("events/latest")
    suspend fun getLatestEvents(@Query("count") count: Int): Response<List<Event>>

    @PATCH("events")
    suspend fun editEvent(@Body event: Event): Response<Event>

    @Multipart
    @POST("media")
    suspend fun saveMedia(@Part file: MultipartBody.Part): Response<Media>
}