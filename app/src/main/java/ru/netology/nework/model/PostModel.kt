package ru.netology.nework.model

import ru.netology.nework.dto.Post

data class PostModel(
    val posts: List<Post> = emptyList()
)