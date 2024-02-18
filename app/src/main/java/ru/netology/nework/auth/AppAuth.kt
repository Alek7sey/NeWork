package ru.netology.nework.auth

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nework.dto.Token
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val tokenKey = "token"
    private val idKey = "id"

    data class AuthState(
        val id: Long = 0,
        val token: String? = null
    )

    private val _authFlow: MutableStateFlow<AuthState>

    init {
        val id = prefs.getLong(idKey, 0L)
        val token = prefs.getString(tokenKey, null)

        if (token == null || id == 0L) {
            _authFlow = MutableStateFlow(AuthState())
            prefs.edit() {
                clear()
                apply()
            }
        } else {
            _authFlow = MutableStateFlow(AuthState(id, token))
        }
    }

    val authFlow: StateFlow<AuthState> = _authFlow.asStateFlow()

    @Synchronized
    fun setAuth(token: Token) {
        _authFlow.value = AuthState(token.id, token.token)
        prefs.edit {
            putLong(idKey, token.id)
            putString(tokenKey, token.token)
            apply()
        }
//        _authFlow.value = token
    }

    @Synchronized
    fun clearAuth() {
        _authFlow.value = AuthState()
        prefs.edit {
            clear()
            apply()
        }
    }

    companion object {
        @Volatile
        private var instance: AppAuth? = null

        fun getInstance(): AppAuth = synchronized(this) {
            instance ?: throw IllegalStateException(
                "AppAuth is not initialized, you must call AppAuth.initializeApp(Context context) first."
            )
        }

        fun initApp(context: Context): AppAuth = instance ?: synchronized(this) {
            instance ?: buildAuth(context).also { instance = it }
        }

        private fun buildAuth(context: Context): AppAuth = AppAuth(context)
    }
}