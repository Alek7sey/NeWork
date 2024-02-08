package ru.netology.nework.model

import ru.netology.nework.dto.Event

data class EventModel (
    val eventsList: List<Event> = emptyList()
)