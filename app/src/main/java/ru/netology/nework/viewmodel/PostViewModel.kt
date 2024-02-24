package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
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
import ru.netology.nework.model.PhotoModel
import ru.netology.nework.model.PostModel
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
    coordinates = null,
    link = null,
    mentionIds = null,
    mentionedMe = false,
    likeOwnerIds = null,
    likedByMe = false,
    attachment = null,
    users = emptyMap()
)


@ExperimentalCoroutinesApi
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {

    val data: Flow<PagingData<Post>> = appAuth
        .authFlow
        .flatMapLatest { token ->
            repository.data.map { posts ->
                posts.map { post ->
                    if (post is Post) {
                            post.copy(ownedByMe = post.authorId == token.id)
                    } else {
                        post
                    }
                }
            }.flowOn(Dispatchers.Default)
        }


    private val _feedState = MutableLiveData<FeedModelState>()
    val feedState: LiveData<FeedModelState>
        get() = _feedState

    private val _photoState = MutableLiveData<PhotoModel?>()
    val photoState: LiveData<PhotoModel?>
        get() = _photoState


    val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: SingleLiveEvent<Unit>
        get() = _postCreated

    val postData: LiveData<PostModel> =
        repository.postData.map(::PostModel).asLiveData(Dispatchers.Default)

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

    fun setPhoto(photoModel: PhotoModel) {
        _photoState.value = photoModel
    }

    fun clearPhoto() {
        _photoState.value = null
    }

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

    fun saveMentionedIds(list: List<Int>) {
        if (edited.value?.mentionIds == list) {
            return
        }
        edited.value = edited.value?.copy(mentionIds = list)
    }

    fun save() {
        edited.value?.let { post ->
            //  postCreated.postValue(Unit)
            viewModelScope.launch {
                try {
                    _photoState.value?.let {
                        repository.saveWithAttachment(post, it.file)
                    } ?: repository.save(post)
                    _postCreated.value = Unit
                    clear()
                    _feedState.value = FeedModelState()
                } catch (e: Exception) {
                    _feedState.value = FeedModelState(error = true)
                }
            }
        }
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
            _feedState.value = FeedModelState(loading = true)
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

    fun clear() {
        edited.value = empty
    }
}