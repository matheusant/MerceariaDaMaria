package com.heracles.troco.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    // Primary (verde mercearia)
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenContainer,
    onPrimaryContainer = OnGreenContainer,

    // Secondary (terracotta — fiado, ações secundárias)
    secondary = TerracottaSecondary,
    onSecondary = TerracottaOnSecondary,
    secondaryContainer = TerracottaContainer,
    onSecondaryContainer = OnTerracottaContainer,

    // Tertiary (reusa terracotta como accent extra)
    tertiary = TerracottaSecondary,
    onTertiary = TerracottaOnSecondary,
    tertiaryContainer = TerracottaContainer,
    onTertiaryContainer = OnTerracottaContainer,

    // Background & Surface
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,

    // Error
    error = ErrorLight,
    onError = GreenOnPrimary,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorLight,

    // Outline
    outline = LightOutline,
    outlineVariant = LightSurfaceVariant,
)

private val DarkColorScheme = darkColorScheme(
    // Primary (verde claro para dark)
    primary = GreenPrimaryDark,
    onPrimary = GreenOnPrimaryDark,
    primaryContainer = GreenContainerDark,
    onPrimaryContainer = OnGreenContainerDark,

    // Secondary (terracotta claro para dark)
    secondary = TerracottaSecondaryDark,
    onSecondary = TerracottaOnSecondaryDark,
    secondaryContainer = TerracottaContainerDark,
    onSecondaryContainer = OnTerracottaContainerDark,

    // Tertiary
    tertiary = TerracottaSecondaryDark,
    onTertiary = TerracottaOnSecondaryDark,
    tertiaryContainer = TerracottaContainerDark,
    onTertiaryContainer = OnTerracottaContainerDark,

    // Background & Surface
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,

    // Error
    error = ErrorDark,
    onError = ErrorContainerDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = ErrorDark,

    // Outline
    outline = DarkOutline,
    outlineVariant = DarkSurfaceVariant,
)

@Composable
fun MerceariaDaMariaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}