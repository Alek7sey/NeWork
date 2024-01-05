package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Job

@Entity
data class JobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val position: String,
    val startDate: String,
    val finishDate: String?,
    val link: String?
) {
    fun toDto() = Job(
        id,
        name,
        position,
        startDate,
        finishDate,
        link,
    )

    companion object {
        fun fromDto(job: Job) =
            JobEntity(
                job.id,
                job.name,
                job.position,
                job.startDate,
                job.finishDate,
                job.link,
            )
    }
}

fun List<JobEntity>.toDto(): List<Job> = map(JobEntity::toDto)
fun List<Job>.toEntity(): List<JobEntity> = map(JobEntity::fromDto)