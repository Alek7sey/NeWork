package ru.netology.nework.model

import android.net.Uri
import ru.netology.nework.dto.AttachmentTypePost
import java.io.File

data class AttachmentModelPost (
    val uri: Uri,
    val file: File,
    val attachmentTypePost: AttachmentTypePost
)