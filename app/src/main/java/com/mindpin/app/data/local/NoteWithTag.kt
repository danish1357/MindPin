package com.mindpin.app.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithTag(
    @Embedded val note: NoteEntity,
    @Relation(
        parentColumn = "tag_id",
        entityColumn = "id"
    )
    val tag: TagEntity?
)
