package com.markmd.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey
    val uri: String,
    val title: String,
    val lastOpened: Long = System.currentTimeMillis(),
    val scrollPosition: Int = 0,
    val cachedContent: String? = null,
    @ColumnInfo(defaultValue = "0")
    val isPinned: Boolean = false,
)
