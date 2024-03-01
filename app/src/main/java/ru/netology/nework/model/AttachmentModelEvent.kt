package ru.netology.nework.model

import android.net.Uri
import ru.netology.nework.dto.AttachmentTypeEvent
import java.io.File

data class AttachmentModelEvent (
    val uri: Uri,
    val file: File,
    val attachmentTypeEvent: AttachmentTypeEvent
)