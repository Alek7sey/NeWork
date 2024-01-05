package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.model.UserModel
import ru.netology.nework.repository.UsersRepository
import ru.netology.nework.state.UserModelState
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val repository: UsersRepository
): ViewModel() {

    val data: LiveData<UserModel> =
        repository.data.map(::UserModel).asLiveData(Dispatchers.Default)

    private val _state = MutableLiveData<UserModelState>()
    val state: LiveData<UserModelState>
        get() = _state

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _state.value = UserModelState(loading = true)
            _state.value = try {
                repository.readAll()
                UserModelState()
            } catch (e: Exception) {
                UserModelState(error = true)
            }
        }
    }
}