package ru.netology.nework.model

import ru.netology.nework.dto.User

data class InvolvedUsersModel(
    val likers: List<User?> = emptyList(),
    val speakers: List<User?> = emptyList(),
    val participants: List<User?> = emptyList(),
    val mentioned: List<User?> = emptyList()
)

enum class InvolvedUserType {
    LIKER,
    SPEAKER,
    PARTICIPANT,
    MENTIONED
}