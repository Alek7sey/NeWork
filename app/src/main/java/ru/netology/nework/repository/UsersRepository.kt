package ru.netology.nework.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.User
import java.io.File

interface UsersRepository {
    val data: Flow<List<User>>
    suspend fun readAll()
    suspend fun setIdAndTokenAuth(id: String, token: String)
    suspend fun registerUser(login: String, name: String, password: String, file: File)
    suspend fun getUser(id: Long): User?

}