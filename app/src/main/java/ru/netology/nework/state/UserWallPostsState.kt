package ru.netology.nework.state

data class UserWallPostsState (
    val refreshing: Boolean = false,
    val loading: Boolean = false,
    val error: Boolean = false
)