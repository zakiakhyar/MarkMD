package com.markmd.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.markmd.data.model.FontFamily

fun buildTypography(fontFamily: FontFamily = FontFamily.SYSTEM_DEFAULT): Typography {
    val ff = appFontFamilyOf(fontFamily)
    return Typography(
        displayLarge = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 45.sp, lineHeight = 52.sp, letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Medium,
            fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Medium,
            fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Medium,
            fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Medium,
            fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
        ),
    )
}
