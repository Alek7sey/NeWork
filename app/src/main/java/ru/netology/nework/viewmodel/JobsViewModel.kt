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
import ru.netology.nework.dto.Job
import ru.netology.nework.model.MyJobModel
import ru.netology.nework.model.UserJobModel
import ru.netology.nework.repository.JobsRepository
import ru.netology.nework.state.MyJobState
import ru.netology.nework.state.UserJobState
import ru.netology.nework.utils.SingleLiveEvent
import ru.netology.nework.utils.convertServerDateToLocalDate
import javax.inject.Inject

val emptyJob = Job(
    id = 0L,
    name = "",
    position = "",
    start = "",
    finish = null,
    link = null
)

@HiltViewModel
class JobsViewModel @Inject constructor(
    private val repository: JobsRepository
) : ViewModel() {
    val dataMyJob = repository.dataMyJobs.map(::MyJobModel).asLiveData(Dispatchers.Default)
    val dataUserJob = repository.dataUserJobs.map(::UserJobModel).asLiveData(Dispatchers.Default)

    private val edited = MutableLiveData(emptyJob)

    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: SingleLiveEvent<Unit>
        get() = _jobCreated

    private val _startDateState = MutableLiveData<String?>()
    val startDateState: LiveData<String?>
        get() = _startDateState

    private val _endDateState = MutableLiveData<String?>()
    val endDateState: LiveData<String?>
        get() = _endDateState

    private val _userIdState = MutableLiveData<Int>()
    val userIdState: LiveData<Int>
        get() = _userIdState

    fun setUserIdState(id: Long) {
        _userIdState.value = id.toInt()
    }

    fun editStartDate(startDate: String) {
        _startDateState.value = startDate
    }

    fun editEndDate(endDate: String) {
        _endDateState.value = endDate
    }

    private val _loadingMyJobState = MutableLiveData(MyJobState())
    val loadingMyJobState: LiveData<MyJobState>
        get() = _loadingMyJobState


    private val _loadingUserJobState = MutableLiveData(UserJobState())
    val loadingUserJobState: LiveData<UserJobState>
        get() = _loadingUserJobState

    init {
        loadMyJobs()
    }

    fun loadMyJobs() {
        viewModelScope.launch {
            _loadingMyJobState.value = MyJobState(loading = true)
            try {
                repository.readMyJobs()
                _loadingMyJobState.value = MyJobState()
            } catch (e: Exception) {
                _loadingMyJobState.value = MyJobState(error = true)
            }
        }
    }

    fun loadUserJobs(id: Long) {
        viewModelScope.launch {
            _loadingUserJobState.value = UserJobState(loading = true)
            try {
                repository.readUserJobs(id)
                _loadingUserJobState.value = UserJobState()
            } catch (e: Exception) {
                _loadingUserJobState.value = UserJobState(error = true)
            }
        }
    }

    fun saveJob() {
        edited.value?.let {
            jobCreated.postValue(Unit)
            viewModelScope.launch {
                _loadingMyJobState.value = MyJobState(loading = true)
                try {
                    repository.saveJob(it)
                    _loadingMyJobState.value = MyJobState()
                    jobCreated.postValue(Unit)
                    edited.value = emptyJob
                } catch (e: Exception) {
                    _loadingMyJobState.value = MyJobState(error = true)
                }
            }
        }
        edited.value = emptyJob
    }

    //  private val serverDateFormat = DateTimeFormatter.ISO_DATE
    //  private val localDateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    fun changeContent(
        nameArg: String,
        positionArg: String,
        link: String?,
        dateStart: String,
        dateFinish: String?
    ) {
        val name = nameArg.trim()
        val position = positionArg.trim()
        val link = link?.trim()
        val start = dateStart.trim()
        val finish = dateFinish?.trim()

        edited.value =
            convertServerDateToLocalDate(start)?.let { startDate ->
                edited.value?.copy(
                    name = nameArg.trim(),
                    position = positionArg.trim(),
                    link = link?.trim()?.takeIf { it.isNotBlank() },
                    start = startDate,
                    finish = finish?.let { convertServerDateToLocalDate(it) }
                )
            }

        if (edited.value?.name == name && edited.value?.position == position) {
            return

        } else if (edited.value?.name != name && edited.value?.position == position) {
            edited.value =
                edited.value?.copy(name = name)

        } else if (edited.value?.name == name && edited.value?.position != position) {
            edited.value =
                edited.value?.copy(position = position)

        } else if (edited.value?.name == name && edited.value?.position == position && edited.value?.link != link) {
            edited.value =
                edited.value?.copy(link = link)
        }
    }

    fun removeJob(id: Long) {
        viewModelScope.launch {
            _loadingMyJobState.value = MyJobState(loading = true)
            try {
                repository.removeJob(id)
                _loadingMyJobState.value = MyJobState()
            } catch (e: Exception) {
                _loadingMyJobState.value = MyJobState(error = true)
            }
        }
    }

}