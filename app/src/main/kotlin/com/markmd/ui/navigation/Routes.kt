package com.markmd.ui.navigation

import android.net.Uri
import kotlinx.serialization.Serializable

sealed class Routes {
    @Serializable
    data object Home : Routes()

    @Serializable
    data class Viewer(val uri: String) : Routes() {
        fun toUri(): Uri = Uri.parse(uri)
    }

    @Serializable
    data class Editor(val uri: String) : Routes() {
        fun toUri(): Uri = Uri.parse(uri)
    }

    @Serializable
    data object Settings : Routes()
}
