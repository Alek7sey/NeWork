package ru.netology.nework.model

import ru.netology.nework.dto.Job

data class MyJobModel (
    val myJobs: List<Job> = emptyList(),
    val empty: Boolean = false
)