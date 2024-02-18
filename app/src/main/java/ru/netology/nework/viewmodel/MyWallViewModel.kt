package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Post
import ru.netology.nework.repository.MyWallRepository
import ru.netology.nework.state.MyWallPostsState
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class MyWallViewModel @Inject constructor(
    private val repository: MyWallRepository,
    appAuth: AppAuth
) : ViewModel() {
    private val cached: Flow<PagingData<Post>> = repository.data.cachedIn(viewModelScope)

    val data: Flow<PagingData<Post>> = appAuth
        .authFlow
        .flatMapLatest { (myId, _) ->
            cached.map { posts ->
                posts.map { item ->
                    if (item is Post) item else item//.copy(ownedByMe = item.authorId == myId)
                }
            }
        }

    private val _myWallPostsState = MutableLiveData(MyWallPostsState())
    val myWallPostsState: LiveData<MyWallPostsState>
        get() = _myWallPostsState

    fun loadMyWallPosts() {
        viewModelScope.launch {
            _myWallPostsState.value = MyWallPostsState(loading = true)
            try {
                repository.readAll()
                _myWallPostsState.value = MyWallPostsState()
            } catch (e: Exception) {
                _myWallPostsState.value = MyWallPostsState(error = true)
            }
        }
    }
}