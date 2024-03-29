package ru.netology.nework.dto

data class User(
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String?,
    val selected: Boolean = false
)