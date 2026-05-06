package com.markmd.domain.usecase

import com.markmd.data.model.TocEntry
import com.markmd.domain.parser.TocParser
import javax.inject.Inject

class ParseTocUseCase @Inject constructor(
    private val parser: TocParser
) {
    operator fun invoke(content: String): List<TocEntry> {
        return parser.parse(content)
    }
}
