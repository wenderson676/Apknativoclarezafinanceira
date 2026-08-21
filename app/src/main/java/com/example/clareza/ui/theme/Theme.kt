package com.example.clareza.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Emerald, Teal & Slate premium branding palette
val EmeraldPrimary = Color(0xFF0D9488)
val EmeraldDark = Color(0xFF042F2E)
val EmeraldLight = Color(0xFF14B8A6)
val EmeraldContainerLight = Color(0xFFCCFBF1)
val EmeraldContainerDark = Color(0xFF115E59)

val TealGradientStart = Color(0xFF064E3B)
val TealGradientEnd = Color(0xFF0F766E)

val Slate950 = Color(0xFF020617)
val Slate900 = Color(0xFF0B1120)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF475569)
val Slate400 = Color(0xFF94A3B8)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)

val RoseExpense = Color(0xFFF43F5E)
val RoseExpenseLight = Color(0xFFFFE4E6)
val AmberPending = Color(0xFFF59E0B)
val AmberPendingLight = Color(0xFFFEF3C7)
val IndigoTransfer = Color(0xFF6366F1)
val IndigoTransferLight = Color(0xFFE0E7FF)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainerLight,
    onPrimaryContainer = Color(0xFF042F2E),
    secondary = IndigoTransfer,
    onSecondary = Color.White,
    secondaryContainer = IndigoTransferLight,
    onSecondaryContainer = Color(0xFF312E81),
    tertiary = AmberPending,
    onTertiary = Color.White,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = Slate600,
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFF1F5F9),
    error = RoseExpense,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = Color(0xFF99F6E4),
    secondary = Color(0xFF818CF8),
    onSecondary = Color(0xFF1E1B4B),
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = AmberPending,
    onTertiary = Color(0xFF451A03),
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B),
    error = RoseExpense,
    onError = Color.White
)

@Composable
fun ClarezaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
