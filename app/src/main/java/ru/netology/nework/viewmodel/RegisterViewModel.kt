package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.repository.UsersRepository
import ru.netology.nework.state.RegisterUserState
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: UsersRepository
) : ViewModel() {
    private val _photoState = MutableLiveData<MediaUpload?>()
    val photoState: LiveData<MediaUpload?>
        get() = _photoState

    private val _registerUserState = MutableLiveData<RegisterUserState>()
    val registerUserState: LiveData<RegisterUserState>
        get() = _registerUserState

    fun setRegisterPhoto(media: MediaUpload) {
        _photoState.value = media
    }

    fun deletePhoto() {
        _photoState.value = null
    }

    fun saveRegisteredUser(login: String, password: String, name: String, file: File) {
        viewModelScope.launch {
            try {
                repository.registerUser(login, name, password, file)
            } catch (e: Exception) {
                _registerUserState.value = RegisterUserState(error = true)
            }
        }
    }

    fun saveRegisteredUserWithoutAvatar(login: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                repository.registerUserWithoutAvatar(login, name, password)
            } catch (e: Exception) {
                _registerUserState.value = RegisterUserState(error = true)
            }
        }
    }
}