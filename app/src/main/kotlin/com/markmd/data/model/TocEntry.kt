package com.markmd.data.model

/**
 * Table of Contents entry.
 */
data class TocEntry(
    val level: Int,
    val title: String,
    val anchor: String
)
