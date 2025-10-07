package com.mindpin.app.model

enum class NoteSort(val storageKey: String) {
    NEWEST("newest"),
    OLDEST("oldest"),
    TAG("tag");

    companion object {
        fun fromKey(key: String): NoteSort = values().firstOrNull { it.storageKey == key } ?: NEWEST
    }
}
