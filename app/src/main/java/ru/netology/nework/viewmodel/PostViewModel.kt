package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import com.yandex.mapkit.geometry.Point
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
import ru.netology.nework.entity.CoordinatesEmbeddable
import ru.netology.nework.model.AttachmentModelPost
import ru.netology.nework.model.PostModel
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.state.FeedModelState
import ru.netology.nework.utils.SingleLiveEvent
import java.time.OffsetDateTime
import javax.inject.Inject

private val empty = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorAvatar = null,
    authorJob = null,
    content = "",
    published = OffsetDateTime.now().toString(),
    coordinates = null,
    link = null,
    mentionIds = null,
    mentionedMe = false,
    likeOwnerIds = null,
    likedByMe = false,
    attachment = null,
    users = emptyMap(),
    likes = 0
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

    private val _attachState = MutableLiveData<AttachmentModelPost?>()
    val attachState: LiveData<AttachmentModelPost?>
        get() = _attachState


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

    fun setAttachment(attachModel: AttachmentModelPost) {
        _attachState.value = attachModel
    }

    fun clearAttachment() {
        _attachState.value = null
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
            viewModelScope.launch {
                try {
                    _attachState.value?.let {
                        repository.saveWithAttachment(post, attachmentModel = it)
                    } ?: repository.save(post)
                    _postCreated.value = Unit
                    _attachState.value = null
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
        }
    }

    fun setCoord(point: Point?) {
        if (point != null) {
            edited.value = edited.value?.copy(
                coordinates = CoordinatesEmbeddable(point.latitude.toString(), point.longitude.toString())
            )
        }
    }

    fun removeCoords() {
        edited.value = edited.value?.copy(
            coordinates = null
        )
    }

    fun clear() {
        edited.value = empty
    }
}