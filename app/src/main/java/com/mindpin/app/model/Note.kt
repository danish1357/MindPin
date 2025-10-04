package com.mindpin.app.model

data class Note(
    val id: Long,
    val content: String,
    val tag: Tag?,
    val createdAt: Long,
    val reminderAt: Long?
)
