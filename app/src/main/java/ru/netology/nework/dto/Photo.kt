package ru.netology.nework.dto

import android.net.Uri
import java.io.File

data class Photo(
    val uri: Uri,
    val file: File
)