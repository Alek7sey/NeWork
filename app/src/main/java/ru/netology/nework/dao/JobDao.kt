package ru.netology.nework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.JobEntity

@Dao
interface JobDao {

    @Query("SELECT * FROM JobEntity ORDER BY id DESC")
    fun getMyJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM JobEntity WHERE id = :id ORDER BY id DESC")
    fun getUsersJob(id: Long): Flow<List<JobEntity>>

    @Query("SELECT * FROM JobEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, JobEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jobList: List<JobEntity>)

    @Query("UPDATE JobEntity SET name = :name, position = :position WHERE id=:id")
    suspend fun updateJob(id: Long, name: String, position: String)

    suspend fun saveJob(job: JobEntity) = if (job.id == 0L) insert(job) else updateJob(job.id, job.name, job.position)

    @Query("DELETE FROM JobEntity WHERE id = :id")
    suspend fun removeJob(id: Long)

    @Query("SELECT * FROM JobEntity WHERE id = :id")
    suspend fun searchJob(id: Long): JobEntity

    @Query("DELETE FROM JobEntity")
    suspend fun removeAll()

}