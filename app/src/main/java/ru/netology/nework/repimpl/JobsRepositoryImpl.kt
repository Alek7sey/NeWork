package ru.netology.nework.repimpl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.nework.api.JobApiService
import ru.netology.nework.dao.JobDao
import ru.netology.nework.dto.Job
import ru.netology.nework.entity.JobEntity
import ru.netology.nework.entity.toDto
import ru.netology.nework.error.*
import ru.netology.nework.repository.JobsRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobsRepositoryImpl @Inject constructor(
    private val jobDao: JobDao,
    private val apiService: JobApiService,
//    userId: Long,
) : JobsRepository {
    override val dataMyJobs: Flow<List<Job>> =
        jobDao.getMyJobs().map(List<JobEntity>::toDto).flowOn(Dispatchers.Default)

    override val dataUserJobs: Flow<List<Job>> =
        jobDao.getMyJobs().map(List<JobEntity>::toDto).flowOn(Dispatchers.Default)

    override suspend fun readMyJobs() {
        try {
            val response = apiService.readMyJobs()
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            val jobs = response.body() ?: throw ApiError(response.message())
            jobDao.removeAll()
            jobDao.insert(jobs.map(JobEntity::fromDto))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun readUserJobs(userId: Long) {
        try {
            val response = apiService.readUserJobs(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            val jobs = response.body() ?: throw ApiError(response.message())
            jobDao.removeAll()
            jobDao.insert(jobs.map(JobEntity::fromDto))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeJob(id: Long) {
        val job = jobDao.searchJob(id)
        try {
            jobDao.removeJob(id)
            val response = apiService.deleteJob(id)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
        } catch (e: IOException) {
            jobDao.insert(job)
            throw NetworkError
        } catch (e: Exception) {
            jobDao.insert(job)
            throw UnknownError
        }
    }

    override suspend fun saveJob(job: Job) {
        try {
            jobDao.insert(JobEntity.fromDto(job))
            val response = apiService.saveJob(job)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            val body = response.body() ?: throw ApiError(response.message())
            jobDao.removeJob(job.id)
            jobDao.insert(JobEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

//    override suspend fun editJob(job: Job) {
//        try {
//            val response = apiService.editJob(job)
//            if (!response.isSuccessful) {
//                throw ApiError(response.message())
//            }
//            val body = response.body() ?: throw ApiError(response.message())
//            jobDao.insert(JobEntity.fromDto(body))
//        } catch (e: AppError) {
//            throw e
//        } catch (e: IOException) {
//            throw NetworkError
//        } catch (e: Exception) {
//            throw UnknownError
//        }
//    }
}