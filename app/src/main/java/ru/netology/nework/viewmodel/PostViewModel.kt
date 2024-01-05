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
import ru.netology.nework.dto.Photo
import ru.netology.nework.dto.Post
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.state.FeedModelState
import ru.netology.nework.utils.SingleLiveEvent
import javax.inject.Inject

private val empty = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorAvatar = null,
    authorJob = null,
    content = "",
    published = "",
    //likes = 0,
    likedByMe = false,
    likeOwnerIds = null,
    mentionedByMe = false,
    mentionedIds = null,
    link = null,
    ownedByMe = false,
    attachment = null,
    coordinates = null,
    users = emptyMap()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {
    private val cached = repository.data.cachedIn(viewModelScope)

    val data: Flow<PagingData<Post>> = appAuth
        .authFlow
        .flatMapLatest { token ->
            cached.map { posts ->
                posts.map {
                    it.copy(ownedByMe = it.authorId == token?.id)
                }
            }.flowOn(Dispatchers.Default)
        }


    private val _feedState = MutableLiveData<FeedModelState>()
    val feedState: LiveData<FeedModelState>
        get() = _feedState

    private val _photoState = MutableLiveData<Photo?>()
    val photoState: LiveData<Photo?>
        get() = _photoState


    private val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: SingleLiveEvent<Unit>
        get() = _postCreated


    init {
         loadPosts()
     }

    fun loadPosts() {
        viewModelScope.launch {
            _feedState.value = FeedModelState(loading = true)
            _feedState.value = try {
                repository.readAll()
               FeedModelState()
            } catch (e: Exception) {
                FeedModelState(error = true)
            }
        }
    }

    fun setPhoto(photo: Photo) {
        _photoState.value = photo
    }

    fun clearPhoto() {
        _photoState.value = null
    }

    /*fun refreshPosts() {
        viewModelScope.launch {
            _state.value = FeedModelState(refreshing = true)
            _state.value = try {
                repository.getAll()
                FeedModelState()
            } catch (e: Exception) {
                FeedModelState(error = true)
            }
        }
    }*/

    fun likeById(post: Post) {
        viewModelScope.launch {
            _feedState.value = FeedModelState(refreshing = true)
            _feedState.value = try {
                repository.likeById(post)
                FeedModelState()
            } catch (e: Exception) {
                FeedModelState(error = true)
            }
        }
    }

    fun save() {
        edited.value?.let { post ->
            postCreated.postValue(Unit)
            viewModelScope.launch {
                try {
                    _photoState.value?.let {
                        repository.saveWithAttachment(post, it.file)
                    } ?: repository.save(post)
                    _feedState.value = FeedModelState()
                } catch (e: Exception) {
                    _feedState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content != text) {
            edited.value = edited.value?.copy(content = text)
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            _feedState.value = FeedModelState(refreshing = true)
            _feedState.value = try {
                repository.removeById(id)
                FeedModelState()
            } catch (e: Exception) {
                FeedModelState(error = true)
            }
        }
    }

    fun shareById(id: Long) {
        viewModelScope.launch {
            _feedState.value = FeedModelState(refreshing = true)
      //     repository.shareById(id)
        }
    }
}