package com.markmd.ui.screen.viewer.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.markmd.data.model.AppTheme
import com.markmd.data.model.ReadingTheme

// ---------------------------------------------------------------------------
// Per-theme color palettes
// ---------------------------------------------------------------------------

object MarkdownPalette {

    // LIGHT — GitHub light default (white background)
    val light = GitHubTokens(
        background       = Color(0xFFFFFFFF),
        text             = Color(0xFF1F2328),
        textMuted        = Color(0xFF636C76),
        codeBlockBg      = Color(0xFFF6F8FA),
        inlineCodeBg     = Color(0x1F818B98),
        codeText         = Color(0xFF1F2328),
        blockquoteBorder = Color(0xFFD0D7DE),
        blockquoteText   = Color(0xFF636C76),
        link             = Color(0xFF0969DA),
        divider          = Color(0xFFD0D7DE),
        tableBg          = Color(0xFFF6F8FA),
        tableText        = Color(0xFF1F2328),
    )

    // DARK — GitHub dark default
    val dark = GitHubTokens(
        background       = Color(0xFF0D1117),
        text             = Color(0xFFE6EDF3),
        textMuted        = Color(0xFF848D97),
        codeBlockBg      = Color(0xFF161B22),
        inlineCodeBg     = Color(0x1F6E7681),
        codeText         = Color(0xFFE6EDF3),
        blockquoteBorder = Color(0xFF3D444D),
        blockquoteText   = Color(0xFF848D97),
        link             = Color(0xFF4493F8),
        divider          = Color(0xFF3D444D),
        tableBg          = Color(0xFF161B22),
        tableText        = Color(0xFFE6EDF3),
    )

    // SEPIA — warm parchment reading theme
    val sepia = GitHubTokens(
        background       = Color(0xFFF5ECD7),
        text             = Color(0xFF3B2F20),
        textMuted        = Color(0xFF7A6652),
        codeBlockBg      = Color(0xFFEDE0C4),
        inlineCodeBg     = Color(0x28A0522D),
        codeText         = Color(0xFF5C3317),
        blockquoteBorder = Color(0xFFC4A882),
        blockquoteText   = Color(0xFF7A6652),
        link             = Color(0xFF8B4513),
        divider          = Color(0xFFD4B896),
        tableBg          = Color(0xFFEDE0C4),
        tableText        = Color(0xFF3B2F20),
    )

    // AMOLED — true black for OLED screens
    val amoled = GitHubTokens(
        background       = Color(0xFF000000),
        text             = Color(0xFFEAEAEA),
        textMuted        = Color(0xFF8B949E),
        codeBlockBg      = Color(0xFF0D0D0D),
        inlineCodeBg     = Color(0x1FFFFFFF),
        codeText         = Color(0xFFEAEAEA),
        blockquoteBorder = Color(0xFF333333),
        blockquoteText   = Color(0xFF8B949E),
        link             = Color(0xFF58A6FF),
        divider          = Color(0xFF333333),
        tableBg          = Color(0xFF0D0D0D),
        tableText        = Color(0xFFEAEAEA),
    )

    // DARK_BLUE — deep navy blue theme
    val darkBlue = GitHubTokens(
        background       = Color(0xFF0D1F2D),
        text             = Color(0xFFCDD9E5),
        textMuted        = Color(0xFF768390),
        codeBlockBg      = Color(0xFF0A1628),
        inlineCodeBg     = Color(0x1F4D8CB5),
        codeText         = Color(0xFFCDD9E5),
        blockquoteBorder = Color(0xFF2D5986),
        blockquoteText   = Color(0xFF768390),
        link             = Color(0xFF539BF5),
        divider          = Color(0xFF1E3A5F),
        tableBg          = Color(0xFF0A1628),
        tableText        = Color(0xFFCDD9E5),
    )

    // DARK_GREEN — Termius-inspired terminal green
    val darkGreen = GitHubTokens(
        background       = Color(0xFF0C1A0C),
        text             = Color(0xFF39FF14),
        textMuted        = Color(0xFF2DB310),
        codeBlockBg      = Color(0xFF0A150A),
        inlineCodeBg     = Color(0x2239FF14),
        codeText         = Color(0xFF7FFF00),
        blockquoteBorder = Color(0xFF1A4D1A),
        blockquoteText   = Color(0xFF2DB310),
        link             = Color(0xFF00FF7F),
        divider          = Color(0xFF1A4D1A),
        tableBg          = Color(0xFF0A150A),
        tableText        = Color(0xFF39FF14),
    )

    // SOLARIZED — Solarized Dark palette
    val solarized = GitHubTokens(
        background       = Color(0xFF002B36),
        text             = Color(0xFF839496),
        textMuted        = Color(0xFF586E75),
        codeBlockBg      = Color(0xFF073642),
        inlineCodeBg     = Color(0x22268BD2),
        codeText         = Color(0xFF93A1A1),
        blockquoteBorder = Color(0xFF2AA198),
        blockquoteText   = Color(0xFF657B83),
        link             = Color(0xFF268BD2),
        divider          = Color(0xFF073642),
        tableBg          = Color(0xFF073642),
        tableText        = Color(0xFF839496),
    )
}

// ---------------------------------------------------------------------------
// Resolved token set
// ---------------------------------------------------------------------------

data class GitHubTokens(
    val background: Color,
    val text: Color,
    val textMuted: Color,
    val codeBlockBg: Color,
    val inlineCodeBg: Color,
    val codeText: Color,
    val blockquoteBorder: Color,
    val blockquoteText: Color,
    val link: Color,
    val divider: Color,
    val tableBg: Color,
    val tableText: Color,
)

@Composable
fun rememberMarkdownTokens(theme: AppTheme): GitHubTokens {
    val isDark = isSystemInDarkTheme()
    return remember(theme, isDark) {
        when (theme) {
            AppTheme.LIGHT     -> MarkdownPalette.light
            AppTheme.DARK      -> MarkdownPalette.dark
            AppTheme.SEPIA     -> MarkdownPalette.sepia
            AppTheme.AMOLED    -> MarkdownPalette.amoled
            AppTheme.DARK_BLUE -> MarkdownPalette.darkBlue
            AppTheme.SYSTEM    -> if (isDark) MarkdownPalette.dark else MarkdownPalette.light
        }
    }
}

@Composable
fun rememberMarkdownTokens(theme: ReadingTheme): GitHubTokens {
    val isDark = isSystemInDarkTheme()
    return remember(theme, isDark) {
        when (theme) {
            ReadingTheme.LIGHT      -> MarkdownPalette.light
            ReadingTheme.DARK       -> MarkdownPalette.dark
            ReadingTheme.SEPIA      -> MarkdownPalette.sepia
            ReadingTheme.AMOLED     -> MarkdownPalette.amoled
            ReadingTheme.DARK_BLUE  -> MarkdownPalette.darkBlue
            ReadingTheme.DARK_GREEN -> MarkdownPalette.darkGreen
            ReadingTheme.SOLARIZED  -> MarkdownPalette.solarized
            ReadingTheme.SYSTEM     -> if (isDark) MarkdownPalette.dark else MarkdownPalette.light
        }
    }
}

// ---------------------------------------------------------------------------
// GitHub-style heading TextStyle factory
//
// GitHub em-based scale (base = user's fontSize setting):
//   h1 = 2.00em  SemiBold  lineHeight 1.25
//   h2 = 1.50em  SemiBold  lineHeight 1.25
//   h3 = 1.25em  SemiBold  lineHeight 1.25
//   h4 = 1.00em  SemiBold  lineHeight 1.25
//   h5 = 0.875em SemiBold  lineHeight 1.25
//   h6 = 0.850em SemiBold  lineHeight 1.25  (color = muted)
// ---------------------------------------------------------------------------

fun githubHeadingStyle(
    level: Int,
    baseFontSize: Int,
    color: Color,
    fontFamily: androidx.compose.ui.text.font.FontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
): TextStyle {
    val sizeSp = when (level) {
        1    -> baseFontSize * 2.00f
        2    -> baseFontSize * 1.50f
        3    -> baseFontSize * 1.25f
        4    -> baseFontSize * 1.00f
        5    -> baseFontSize * 0.875f
        else -> baseFontSize * 0.850f
    }
    return TextStyle(
        fontSize      = sizeSp.sp,
        fontFamily    = fontFamily,
        fontWeight    = FontWeight.SemiBold,
        lineHeight    = (sizeSp * 1.25f).sp,
        letterSpacing = 0.sp,
        color         = color,
    )
}
