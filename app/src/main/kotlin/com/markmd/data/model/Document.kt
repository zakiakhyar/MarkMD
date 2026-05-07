package com.markmd.data.model

import android.net.Uri

/**
 * Domain model representing a markdown document.
 */
data class Document(
    val uri: Uri,
    val title: String,
    val content: String,
    val lastModified: Long = System.currentTimeMillis(),
    val scrollPosition: Int = 0,
    val isPinned: Boolean = false,
)
