package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Post
import ru.netology.nework.repository.UserWallRepository
import ru.netology.nework.state.UserWallPostsState
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class UserWallViewModel @Inject constructor(
    private val repository: UserWallRepository,
    appAuth: AppAuth
) : ViewModel() {
    private val cached: Flow<PagingData<Post>> = repository.data.cachedIn(viewModelScope)

    val data: Flow<PagingData<Post>> = appAuth
        .authFlow
        .flatMapLatest { token ->
            cached.map { posts ->
                posts.map { item ->
                    if (item is Post) {
                        //item.copy(authorId = token.id)
                        item
                    } else {
                        item
                    }
                }
            }.flowOn(Dispatchers.Default)
        }


    private val _userWallPostsState = MutableLiveData(UserWallPostsState())
    val userWallPostsState: LiveData<UserWallPostsState>
        get() = _userWallPostsState

    fun loadUserWallPosts(authorId: Long) {
        viewModelScope.launch {
            _userWallPostsState.value = UserWallPostsState(loading = true)
            try {
                repository.readAllFromUserWall(authorId)
                _userWallPostsState.value = UserWallPostsState()
            } catch (e: Exception) {
                _userWallPostsState.value = UserWallPostsState(error = true)
            }
        }
    }
}