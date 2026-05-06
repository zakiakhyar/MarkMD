package com.markmd.domain.parser

import com.markmd.data.model.TocEntry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TocParser @Inject constructor() {

    fun parse(content: String): List<TocEntry> {
        return HEADING_REGEX.findAll(content).map { match ->
            val level = match.groupValues[1].length
            val title = match.groupValues[2].trim()
            TocEntry(level, title, generateAnchor(title))
        }.toList()
    }

    companion object {
        val HEADING_REGEX = Regex("""^(#{1,6})\s+(.+)$""", RegexOption.MULTILINE)

        fun generateAnchor(title: String): String {
            return title.lowercase()
                .replace(Regex("[^a-z0-9\\s-]"), "")
                .replace(Regex("\\s+"), "-")
                .trim('-')
        }
    }
}
