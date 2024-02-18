package ru.netology.nework.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.dto.Job


interface JobApiService {
    @GET("my/jobs")
    suspend fun readMyJobs(): Response<List<Job>>

    @POST("my/jobs")
    suspend fun saveJob(@Body job: Job): Response<Job>

    @DELETE("my/jobs/{id}")
    suspend fun deleteJob(@Path("id") id: Long): Response<Unit>

    @GET("{userId}/jobs")
    suspend fun readUserJobs(@Path("userId") userId: Long): Response<List<Job>>

    @PATCH("my/jobs")
    suspend fun editJob(@Body job: Job): Response<Job>
}