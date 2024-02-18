package ru.netology.nework.state

data class MyJobState(
    val refreshing: Boolean = false,
    val loading: Boolean = false,
    val error: Boolean = false
)
