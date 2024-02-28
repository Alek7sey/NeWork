package ru.netology.nework.repimpl

import android.content.Context
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.R
import ru.netology.nework.api.UserApiService
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dao.UserDao
import ru.netology.nework.dto.User
import ru.netology.nework.entity.UserEntity
import ru.netology.nework.entity.toDto
import ru.netology.nework.error.*
import ru.netology.nework.repository.UsersRepository
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepositoryImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
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

    override suspend fun setIdAndTokenAuth(login: String, pass: String) {
        try {
            val response = apiService.authUser(login, pass)
            if (!response.isSuccessful) {
                when (response.code()) {
                    400 -> {
                        Toast.makeText(context, context.getString(R.string.incorrect_username_or_password), Toast.LENGTH_LONG).show()
                    }

                    404 -> {
                        Toast.makeText(context, context.getString(R.string.user_is_not_registered), Toast.LENGTH_LONG).show()
                    }
                    else -> Toast.makeText(context, context.getString(R.string.unknown_error), Toast.LENGTH_LONG).show()
                }
                return
            }
            val result = response.body() ?: throw RuntimeException("Body is null")
            appAuth.setAuth(result)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun registerUser(login: String, name: String, password: String, file: File) {
        val media = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())

        try {
            val response = apiService.regUser(login, password, name, media)
            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> {
                        Toast.makeText(context, context.getString(R.string.user_is_already_registered), Toast.LENGTH_LONG).show()
                    }

                    415 -> {
                        Toast.makeText(context, context.getString(R.string.incorrect_photo_format), Toast.LENGTH_LONG).show()
                    }
                    else -> Toast.makeText(context, context.getString(R.string.unknown_error), Toast.LENGTH_LONG).show()
                }
               return
            }

            val result = response.body() ?: throw RuntimeException("Body is null")
            appAuth.setAuth(result)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun registerUserWithoutAvatar(login: String, name: String, password: String) {

        try {
            val response = apiService.registerUserWithoutAvatar(login, password, name)
            if (!response.isSuccessful) {
                when (response.code()) {
                    403 -> {
                        Toast.makeText(context, context.getString(R.string.user_is_already_registered), Toast.LENGTH_LONG).show()
                    }
                    else -> Toast.makeText(context, context.getString(R.string.unknown_error), Toast.LENGTH_LONG).show()
                }
                return
            }
            val result = response.body() ?: throw RuntimeException("Body is null")
            appAuth.setAuth(result)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getUser(id: Long): User? {
        try {
            val response = apiService.getUser(id)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            return response.body()

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}