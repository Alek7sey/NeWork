package ru.netology.nework.model

import ru.netology.nework.dto.User

data class UserModel(
    val users: List<User> = emptyList(),
    val empty: Boolean = false
)