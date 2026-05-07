package com.markmd.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.markmd.data.model.AppTheme
import com.markmd.data.model.FontFamily

private val LightColors = lightColorScheme(
    primary               = light_primary,
    onPrimary             = light_onPrimary,
    primaryContainer      = light_primaryContainer,
    onPrimaryContainer    = light_onPrimaryContainer,
    secondary             = light_secondary,
    onSecondary           = light_onSecondary,
    secondaryContainer    = light_secondaryContainer,
    onSecondaryContainer  = light_onSecondaryContainer,
    tertiary              = light_tertiary,
    onTertiary            = light_onTertiary,
    tertiaryContainer     = light_tertiaryContainer,
    onTertiaryContainer   = light_onTertiaryContainer,
    error                 = light_error,
    errorContainer        = light_errorContainer,
    onError               = light_onError,
    onErrorContainer      = light_onErrorContainer,
    background            = light_background,
    onBackground          = light_onBackground,
    surface               = light_surface,
    onSurface             = light_onSurface,
    surfaceVariant        = light_surfaceVariant,
    onSurfaceVariant      = light_onSurfaceVariant,
    outline               = light_outline,
    inverseOnSurface      = light_inverseOnSurface,
    inverseSurface        = light_inverseSurface,
    inversePrimary        = light_inversePrimary,
)

private val DarkColors = darkColorScheme(
    primary               = dark_primary,
    onPrimary             = dark_onPrimary,
    primaryContainer      = dark_primaryContainer,
    onPrimaryContainer    = dark_onPrimaryContainer,
    secondary             = dark_secondary,
    onSecondary           = dark_onSecondary,
    secondaryContainer    = dark_secondaryContainer,
    onSecondaryContainer  = dark_onSecondaryContainer,
    tertiary              = dark_tertiary,
    onTertiary            = dark_onTertiary,
    tertiaryContainer     = dark_tertiaryContainer,
    onTertiaryContainer   = dark_onTertiaryContainer,
    error                 = dark_error,
    errorContainer        = dark_errorContainer,
    onError               = dark_onError,
    onErrorContainer      = dark_onErrorContainer,
    background            = dark_background,
    onBackground          = dark_onBackground,
    surface               = dark_surface,
    onSurface             = dark_onSurface,
    surfaceVariant        = dark_surfaceVariant,
    onSurfaceVariant      = dark_onSurfaceVariant,
    outline               = dark_outline,
    inverseOnSurface      = dark_inverseOnSurface,
    inverseSurface        = dark_inverseSurface,
    inversePrimary        = dark_inversePrimary,
)

private val SepiaColors = lightColorScheme(
    primary               = sepia_primary,
    onPrimary             = sepia_onPrimary,
    primaryContainer      = sepia_primaryContainer,
    onPrimaryContainer    = sepia_onBackground,
    secondary             = sepia_onSurfaceVariant,
    onSecondary           = sepia_onPrimary,
    secondaryContainer    = sepia_surfaceVariant,
    onSecondaryContainer  = sepia_onBackground,
    error                 = ErrorRed,
    errorContainer        = light_errorContainer,
    onError               = White,
    onErrorContainer      = light_onErrorContainer,
    background            = sepia_background,
    onBackground          = sepia_onBackground,
    surface               = sepia_surface,
    onSurface             = sepia_onSurface,
    surfaceVariant        = sepia_surfaceVariant,
    onSurfaceVariant      = sepia_onSurfaceVariant,
    outline               = sepia_outline,
)

private val AmoledColors = darkColorScheme(
    primary               = amoled_primary,
    onPrimary             = amoled_onPrimary,
    primaryContainer      = amoled_surfaceVariant,
    onPrimaryContainer    = amoled_onSurface,
    secondary             = amoled_onSurfaceVariant,
    onSecondary           = amoled_onPrimary,
    secondaryContainer    = amoled_surfaceVariant,
    onSecondaryContainer  = amoled_onSurface,
    error                 = ErrorRed,
    errorContainer        = ErrorRedDim,
    onError               = White,
    onErrorContainer      = dark_onErrorContainer,
    background            = amoled_background,
    onBackground          = amoled_onSurface,
    surface               = amoled_surface,
    onSurface             = amoled_onSurface,
    surfaceVariant        = amoled_surfaceVariant,
    onSurfaceVariant      = amoled_onSurfaceVariant,
    outline               = amoled_outline,
)

@Composable
fun MarkMDTheme(
    theme: AppTheme = AppTheme.SYSTEM,
    fontFamily: FontFamily = FontFamily.SYSTEM_DEFAULT,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (theme) {
        AppTheme.DARK, AppTheme.AMOLED, AppTheme.DARK_BLUE -> true
        AppTheme.LIGHT, AppTheme.SEPIA -> false
        AppTheme.SYSTEM -> isSystemDark
    }

    val colorScheme = when (theme) {
        AppTheme.LIGHT     -> LightColors
        AppTheme.DARK      -> DarkColors
        AppTheme.SEPIA     -> SepiaColors
        AppTheme.AMOLED    -> AmoledColors
        AppTheme.DARK_BLUE -> DarkColors
        AppTheme.SYSTEM    -> if (isSystemDark) DarkColors else LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = buildTypography(fontFamily),
        content     = content
    )
}
