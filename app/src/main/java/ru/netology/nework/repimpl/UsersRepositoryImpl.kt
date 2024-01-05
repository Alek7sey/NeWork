package ru.netology.nework.repimpl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nework.api.UserApiService
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dao.UserDao
import ru.netology.nework.dto.User
import ru.netology.nework.entity.UserEntity
import ru.netology.nework.entity.toDto
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import ru.netology.nework.repository.UsersRepository
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val apiService: UserApiService,
) : UsersRepository {
    override val data: Flow<List<User>> = userDao.getAllUsers().map(List<UserEntity>::toDto).flowOn(Dispatchers.Default)

    @Inject
    lateinit var appAuth: AppAuth

    override suspend fun readAll() {
        try {
            val response = apiService.readAllUsers()
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            val users = response.body() ?: throw ApiError(response.message())
            userDao.removeAll()
            userDao.insert(users.map(UserEntity::fromDto))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun setIdAndTokenAuth(id: String, token: String) {
        try {

            val response = apiService.authUser(id, token)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }

            val result = response.body() ?: throw ApiError(response.message())
            appAuth.setAuth(result)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun registerUser(login: String, name: String, password: String, file: File) {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", file.name, file.asRequestBody()
            )
            val userLogin = login.toRequestBody("text/plain".toMediaType())
            val userPassword = password.toRequestBody("text/plain".toMediaType())
            val userName = name.toRequestBody("text/plain".toMediaType())

            val response = apiService.regUser(userLogin, userPassword, userName, media)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }

            val result = response.body() ?: throw ApiError(response.message())
            appAuth.setAuth(result)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getUser(id: Long) {
        try {
            val response = apiService.getUser(id)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            val user = response.body() ?: throw ApiError(response.message())
//            userDao.removeAll()
            userDao.insert(UserEntity.fromDto(user))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}