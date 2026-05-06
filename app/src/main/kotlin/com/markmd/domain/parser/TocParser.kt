package com.markmd.domain.parser

import com.markmd.data.model.TocEntry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TocParser @Inject constructor() {

    fun parse(content: String): List<TocEntry> {
        val fenceRegex = Regex("""^(`{3,}|~{3,}).*$""", RegexOption.MULTILINE)
        val fencedRanges = mutableListOf<IntRange>()
        var fenceStart: Int? = null
        var fenceMarker: String? = null
        for (fm in fenceRegex.findAll(content)) {
            val marker = fm.groupValues[1]
            if (fenceStart == null) {
                fenceStart = fm.range.first
                fenceMarker = marker
            } else if (fenceMarker != null && marker.startsWith(fenceMarker!![0]) && marker.length >= fenceMarker!!.length) {
                fencedRanges += fenceStart!!..fm.range.last
                fenceStart = null
                fenceMarker = null
            }
        }
        return HEADING_REGEX.findAll(content)
            .filter { m -> fencedRanges.none { range -> m.range.first in range } }
            .map { match ->
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
