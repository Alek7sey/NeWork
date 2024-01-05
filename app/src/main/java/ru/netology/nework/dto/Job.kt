package ru.netology.nework.dto

data class Job(
    val id: Long,
    val name: String,
    val position: String,
    val startDate: String,
    val finishDate: String?,
    val link: String?
)