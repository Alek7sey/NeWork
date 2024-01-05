package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.repository.UsersRepository
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: UsersRepository
) : ViewModel() {
    private val _image = MutableLiveData<MediaUpload>()
    val image: LiveData<MediaUpload>
        get() = _image

    fun setRegisterImage(media: MediaUpload) {
        _image.value = media
    }

    fun saveRegisteredUser(login: String, password: String, name: String, file: File) {
        viewModelScope.launch {
            try {
                repository.registerUser(login, name, password, file)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}