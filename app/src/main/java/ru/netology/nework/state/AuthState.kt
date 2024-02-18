package ru.netology.nework.state

data class AuthState (
    val loading: Boolean = false,
    val error: Boolean = false
)