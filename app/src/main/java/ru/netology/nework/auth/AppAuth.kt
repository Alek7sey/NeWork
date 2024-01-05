package ru.netology.nework.auth

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nework.dto.Token
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    context: Context
) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val tokenKey = "token"
    private val idKey = "id"

    data class AuthState(
        val id: Long = 0,
        val token: String? = null
    )

    private val _authFlow = MutableStateFlow<Token?>(null)
    val authFlow = _authFlow.asStateFlow()

    init {
        val id = prefs.getLong(idKey, 0L)
        val token = prefs.getString(tokenKey, null)

        if (token == null || id == 0L) {
            prefs.edit() { clear() }
        } else {
            _authFlow.value = Token(id, token)
        }
    }

    fun setAuth(token: Token) {
        prefs.edit {
            putLong(idKey, token.id)
            putString(tokenKey, token.token)
        }
        _authFlow.value = token
    }

    fun clear() {
        prefs.edit { clear() }
        _authFlow.value = null
    }
}