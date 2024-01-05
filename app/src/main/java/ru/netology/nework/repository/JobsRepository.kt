package ru.netology.nework.repository

import ru.netology.nework.dto.Job
import kotlinx.coroutines.flow.Flow

interface JobsRepository {
    val dataMyJobs: Flow<List<Job>>
    val dataUserJobs: Flow<List<Job>>
    suspend fun readMyJobs()
    suspend fun readUserJobs(userId: Long)
    suspend fun removeJob(id: Long)
    suspend fun saveJob(job: Job)
//    suspend fun editJob(job: Job)
}