package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.repository.UsersRepository
import ru.netology.nework.state.FeedModelState

import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UsersRepository
) : ViewModel() {
    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState>
        get() = _state

    fun saveIdAndToken(id: String, token: String) {
        viewModelScope.launch {
            _state.value = FeedModelState(refreshing = true)
            try {
                repository.setIdAndTokenAuth(id, token)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
                e.printStackTrace()
            }
        }
    }
}