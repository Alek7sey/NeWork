package ru.netology.nework.state

data class UserJobState (
    val refreshing: Boolean = false,
    val loading: Boolean = false,
    val error: Boolean = false
)