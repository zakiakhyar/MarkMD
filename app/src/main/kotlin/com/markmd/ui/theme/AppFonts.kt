package com.markmd.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.markmd.R
import com.markmd.data.model.FontFamily as AppFontFamily

object AppFonts {
    val lora by lazy {
        FontFamily(
            Font(R.font.lora_regular,  FontWeight.Normal),
            Font(R.font.lora_italic,   FontWeight.Normal,   FontStyle.Italic),
            Font(R.font.lora_semibold, FontWeight.SemiBold),
            Font(R.font.lora_bold,     FontWeight.Bold),
        )
    }

    val merriweather by lazy {
        FontFamily(
            Font(R.font.merriweather_regular, FontWeight.Normal),
            Font(R.font.merriweather_italic,  FontWeight.Normal, FontStyle.Italic),
            Font(R.font.merriweather_bold,     FontWeight.Bold),
        )
    }

    val atkinsonHl by lazy {
        FontFamily(
            Font(R.font.atkinson_regular,     FontWeight.Normal),
            Font(R.font.atkinson_italic,      FontWeight.Normal, FontStyle.Italic),
            Font(R.font.atkinson_bold,        FontWeight.Bold),
            Font(R.font.atkinson_bold_italic, FontWeight.Bold,   FontStyle.Italic),
        )
    }

    val literata by lazy {
        FontFamily(
            Font(R.font.literata_regular, FontWeight.Normal),
            Font(R.font.literata_italic,  FontWeight.Normal, FontStyle.Italic),
            Font(R.font.literata_bold,    FontWeight.Bold),
        )
    }

    val sourceCodePro by lazy {
        FontFamily(
            Font(R.font.sourcecodepro_regular, FontWeight.Normal),
            Font(R.font.sourcecodepro_italic,  FontWeight.Normal, FontStyle.Italic),
            Font(R.font.sourcecodepro_bold,    FontWeight.Bold),
        )
    }
}

fun appFontFamilyOf(family: AppFontFamily): FontFamily = when (family) {
    AppFontFamily.SYSTEM_DEFAULT  -> FontFamily.Default
    AppFontFamily.SERIF           -> AppFonts.lora
    AppFontFamily.GEORGIA         -> AppFonts.merriweather
    AppFontFamily.LITERATA        -> AppFonts.literata
    AppFontFamily.OPEN_DYSLEXIC   -> AppFonts.atkinsonHl
    AppFontFamily.SOURCE_CODE_PRO -> AppFonts.sourceCodePro
}
