package ru.netology.nework.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Event
import ru.netology.nework.entity.CoordinatesEventEmbeddable
import ru.netology.nework.model.AttachmentModelEvent
import ru.netology.nework.model.EventModel
import ru.netology.nework.model.InvolvedUserType
import ru.netology.nework.model.InvolvedUsersModel
import ru.netology.nework.repository.EventsRepository
import ru.netology.nework.repository.UsersRepository
import ru.netology.nework.state.EventModelState
import ru.netology.nework.utils.SingleLiveEvent
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

private val emptyEvent = Event(
    id = 0,
    authorId = 0,
    author = "",
    authorJob = "",
    authorAvatar = null,
    content = "",
    datetime = "",
    published = "",//OffsetDateTime.now().toString(),
    coordinates = null,
    type = "",
    likeOwnerIds = null,
    likedByMe = false,
    speakerIds = null,
    participantsIds = null,
    participatedByMe = false,
    attachment = null,
    link = "",
    users = emptyMap(),
    ownedByMe = false
)

@HiltViewModel
@ExperimentalCoroutinesApi
class EventsViewModel @Inject constructor(
    private val repository: EventsRepository,
    private val userRepository: UsersRepository,
    appAuth: AppAuth
) : ViewModel() {

    private val cached = repository.data.cachedIn(viewModelScope)

    val data: Flow<PagingData<Event>> = appAuth
        .authFlow
        .flatMapLatest { token ->
            cached.map { events ->
                events.map { event ->
                    if (event is Event) {
                        event.copy(ownedByMe = event.authorId == token.id)
                    } else {
                        event
                    }
                }
            }.flowOn(Dispatchers.Default)
        }

    private val _eventState = MutableLiveData<EventModelState>()
    val eventState: LiveData<EventModelState>
        get() = _eventState

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: SingleLiveEvent<Unit>
        get() = _eventCreated

    private val _eventEdited = SingleLiveEvent<Unit>()
    val eventEdited: SingleLiveEvent<Unit>
        get() = _eventEdited

    private val _attachState = MutableLiveData<AttachmentModelEvent?>()
    val attachState: LiveData<AttachmentModelEvent?>
        get() = _attachState

    val edited = MutableLiveData(emptyEvent)

    private val _eventTypesState = MutableLiveData<String>()
    val eventTypesState: LiveData<String>
        get() = _eventTypesState

    private val _dateTimeState = MutableLiveData<String>()
    val dateTimeState: LiveData<String>
        get() = _dateTimeState

    val involvedData = MutableLiveData(InvolvedUsersModel())

    fun editType(type: String) {
        _eventTypesState.value = type
    }

    fun editDateTime(dateTime: String) {
        _dateTimeState.value = dateTime
    }

    val eventDetailsData: LiveData<EventModel> =
        repository.getData.map(::EventModel).asLiveData(Dispatchers.Default)


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

    fun setAttachment(attachModel: AttachmentModelEvent) {
        _attachState.value = attachModel
    }

    fun clearAttachment() {
        _attachState.value = null
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
            viewModelScope.launch {
                try {
                    _attachState.value?.let {
                        repository.saveWithAttachment(event, it)
                    } ?: repository.save(event)
                    _eventCreated.value = Unit
                    clear()
                    _eventState.value = EventModelState()
                } catch (e: Exception) {
                    _eventState.value = EventModelState(error = true)
                }
            }
        }
    }

    fun edit(event: Event) {
        edited.value = event
    }

    private val serverDate = DateTimeFormatter.ISO_DATE_TIME
    private val localDateFormat: DateTimeFormatter = DateTimeFormatter
        .ofPattern("dd.MM.yyyy HH:mm", Locale.US) //:ss.SSS
//        .withLocale(Locale.getDefault())
//        .withZone(ZoneId.systemDefault())

    fun changeContent(content: String, datetime: String, eventType: String) {
        if (edited.value?.content != content.trim()) {
            val localDateTime = localDateFormat.parse(datetime.trim()) //+ ":00.000"
            edited.value =
                edited.value?.copy(
                    content = content.trim(),
                    datetime = serverDate.format(localDateTime),
                    type = eventType.trim()
                )
            Log.d("Logging", "DateTime: ${edited.value?.datetime}")
        }
    }


    fun removeById(id: Long) {
        viewModelScope.launch {
            _eventState.value = EventModelState(loading = true)
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
        }
    }

    fun setCoord(point: Point?) {
        if (point != null) {
            edited.value = edited.value?.copy(
                coordinates = CoordinatesEventEmbeddable(point.latitude.toString(), point.longitude.toString())
            )
        }
    }

    fun removeCoords() {
        edited.value = edited.value?.copy(
            coordinates = null
        )
    }

    fun saveMentionedIds(list: List<Int>) {
        if (edited.value?.participantsIds == list) {
            return
        }
        edited.value = edited.value?.copy(participantsIds = list)
    }

    suspend fun getInvolvedUser(involved: List<Int>, involvedUserType: InvolvedUserType) {
        val list = involved.let {
            if (it.size > 4) it.take(5) else it
        }.map {
            viewModelScope.async { userRepository.getUser(it.toLong()) }
        }.awaitAll()

        synchronized(involvedData) {
            when (involvedUserType) {
                InvolvedUserType.LIKER -> {
                    involvedData.value = involvedData.value?.copy(likers = list)
                }

                InvolvedUserType.SPEAKER -> {
                    involvedData.value = involvedData.value?.copy(speakers = list)
                }

                InvolvedUserType.PARTICIPANT -> {
                    involvedData.value = involvedData.value?.copy(participants = list)
                }

                else -> return
            }
        }
    }

    fun clearInvolved() {
        involvedData.value = InvolvedUsersModel()
    }
    fun clear() {
        edited.value = emptyEvent
    }
}
