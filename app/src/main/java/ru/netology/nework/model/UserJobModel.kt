package ru.netology.nework.model

import ru.netology.nework.dto.Job

data class UserJobModel (
    val userJobs: List<Job> = emptyList(),
    val empty: Boolean = false
)