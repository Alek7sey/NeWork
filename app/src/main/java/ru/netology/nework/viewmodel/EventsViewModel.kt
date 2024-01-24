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
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Photo
import ru.netology.nework.repository.EventsRepository
import ru.netology.nework.state.EventModelState
import ru.netology.nework.utils.SingleLiveEvent
import javax.inject.Inject

private val emptyEvent = Event(
    id = 0,
    authorId = 0,
    author = "",
    authorJob = "",
    authorAvatar = null,
    content = "",
    datetime = "",
    published = "",
    coordinates = null,
    type = "",
    likeOwnerIds = null,
    likedByMe = false,
    speakerIds = null,
    participantsIds = null,
    participatedByMe = false,
    attachment = null,
    link = "",
    users = emptyMap()
)

@HiltViewModel
@ExperimentalCoroutinesApi
class EventsViewModel @Inject constructor(
    private val repository: EventsRepository,
    appAuth: AppAuth
) : ViewModel() {

    private val cached = repository.data.cachedIn(viewModelScope)

    val data: Flow<PagingData<Event>> = appAuth
        .authFlow
        .flatMapLatest { token ->
            cached.map { events ->
                events.map { event ->
                    if (event is Event) {
                        //       event.copy(ownedByMe = post.authorId == token?.id)
                        event
                    } else {
                        event
                    }
                }
            }.flowOn(Dispatchers.Default)
        }


    private val _eventState = MutableLiveData<EventModelState>()
    val eventState: LiveData<EventModelState>
        get() = _eventState

    private val _photoState = MutableLiveData<Photo?>()
    val photoState: LiveData<Photo?>
        get() = _photoState

    private val edited = MutableLiveData(emptyEvent)

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: SingleLiveEvent<Unit>
        get() = _eventCreated


    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _eventState.value = EventModelState(loading = true)
            _eventState.value = try {
                repository.readAll()
                EventModelState()
            } catch (e: Exception) {
                EventModelState(error = true)
            }
        }
    }

    fun setPhoto(photo: Photo) {
        _photoState.value = photo
    }

    fun clearPhoto() {
        _photoState.value = null
    }

    fun likeById(event: Event) {
        viewModelScope.launch {
            _eventState.value = EventModelState(refreshing = true)
            _eventState.value = try {
                repository.likeById(event)
                EventModelState()
            } catch (e: Exception) {
                EventModelState(error = true)
            }
        }
    }

    fun participatedById(event: Event) {
        viewModelScope.launch {
            _eventState.value = EventModelState(refreshing = true)
            _eventState.value = try {
                repository.participatedById(event)
                EventModelState()
            } catch (e: Exception) {
                EventModelState(error = true)
            }
        }
    }

    fun save() {
        edited.value?.let { event ->
            eventCreated.postValue(Unit)
            viewModelScope.launch {
                try {
                    _photoState.value?.let {
                        repository.saveWithAttachment(event, it.file)
                    } ?: repository.save(event)
                    _eventState.value = EventModelState()
                } catch (e: Exception) {
                    _eventState.value = EventModelState(error = true)
                }
            }
        }
        edited.value = emptyEvent
    }

    fun edit(event: Event) {
        edited.value = event
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content != text) {
            edited.value = edited.value?.copy(content = text)
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            _eventState.value = EventModelState(refreshing = true)
            _eventState.value = try {
                repository.removeById(id)
                EventModelState()
            } catch (e: Exception) {
                EventModelState(error = true)
            }
        }
    }

    fun shareById(id: Long) {
        viewModelScope.launch {
            _eventState.value = EventModelState(refreshing = true)
            //     repository.shareById(id)
        }
    }
}
