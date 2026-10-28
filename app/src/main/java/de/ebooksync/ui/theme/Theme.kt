package de.ebooksync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/** M3 Expressive theme using the Pixel's dynamic (wallpaper) colors. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EbookSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}
