package dev.catsuperberg.bingogen.client.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)


data class ExtendedColors(
    val material: ColorScheme,
    val brightRed: Color,
    val onBrightRed: Color,
    val elevatedTertiaryContainer: Color,
    val onElevatedTertiaryContainer: Color,
    val failed: Color,
    val onFailed: Color,
    val failedContainer: Color,
    val onFailedContainer: Color,
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
)

private val ExtendedDarkColorScheme = ExtendedColors(
    material = DarkColorScheme,
    brightRed = dark_BrightRed,
    onBrightRed = dark_onBrightRed,
    elevatedTertiaryContainer = DarkColorScheme.tertiaryContainer.elevate(0.4f),
    onElevatedTertiaryContainer = DarkColorScheme.onTertiaryContainer,
    failed = dark_BrightRed,
    onFailed = dark_onBrightRed,
    failedContainer = dark_BrightRedContainer,
    onFailedContainer = dark_onBrightRedContainer,
    success = dark_BrightGreen,
    onSuccess = dark_onBrightGreen,
    successContainer = dark_BrightGreenContainer,
    onSuccessContainer = dark_onBrightGreenContainer,
)

private val ExtendedLightColorScheme = ExtendedColors(
    material = LightColorScheme,
    brightRed = light_BrightRed,
    onBrightRed = light_onBrightRed,
    elevatedTertiaryContainer = LightColorScheme.tertiaryContainer.elevate(0.25f),
    onElevatedTertiaryContainer = LightColorScheme.onTertiaryContainer,
    failed = light_BrightRed,
    onFailed = light_onBrightRed,
    failedContainer = light_BrightRedContainer,
    onFailedContainer = light_onBrightRedContainer,
    success = light_BrightGreen,
    onSuccess = light_onBrightGreen,
    successContainer = dark_BrightGreen,
    onSuccessContainer = dark_onBrightGreen,
)

private fun Color.elevate(amount: Float): Color {
    val red = (this.red * (1 - amount)).coerceIn(0f, 1f)
    val green = (this.green * (1 - amount)).coerceIn(0f, 1f)
    val blue = (this.blue * (1 - amount)).coerceIn(0f, 1f)

    return Color(red, green, blue, this.alpha)
}

private val LocalColors = staticCompositionLocalOf { ExtendedDarkColorScheme }
private val LocalTypography = staticCompositionLocalOf { ExtendedTypography }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalColors.current

val MaterialTheme.extendedTypography: AdditionalTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalTypography.current

@Composable
fun BingogenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> ExtendedDarkColorScheme
        else -> ExtendedLightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.material.outline.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = darkTheme
        }
    }

    CompositionLocalProvider(LocalColors provides colorScheme, LocalTypography provides ExtendedTypography) {
        MaterialTheme(
            colorScheme = colorScheme.material,
            typography = Typography,
            content = content
        )
    }
}
